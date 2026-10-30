/**
 * ModalTambahAktivitas.jsx
 * Web Admin Modal untuk Seleksi Aktivitas Kerja Lapangan
 * Mendukung filter cerdas sesuai Unit Alat Berat dan Kategori Status:
 * [Semua], [Operasi], [Standby], [Breakdown].
 */
import React, { useState, useMemo } from 'react';

export default function ModalTambahAktivitas({
  isOpen,
  onClose,
  selectedUnit,
  allActivities = [],
  onSelectActivity
}) {
  const [selectedCategory, setSelectedCategory] = useState('SEMUA');
  const [searchQuery, setSearchQuery] = useState('');

  // Saring aktivitas berdasarkan Unit terpilih & Kategori Status (Operasi, Standby, Breakdown)
  const filteredActivities = useMemo(() => {
    if (!selectedUnit) return allActivities;
    const unitType = (selectedUnit.jenis_unit || selectedUnit.unitType || '').trim().toUpperCase();
    const unitModel = (selectedUnit.brand_model || selectedUnit.brandModel || '').trim().toUpperCase();

    return allActivities.filter(act => {
      const actCat = (act.kategori_status || act.kategori || '').trim().toUpperCase();
      const matchesCategory = 
        selectedCategory === 'SEMUA' ? true :
        selectedCategory === 'OPERASI' ? (actCat.includes('OPERASI') || actCat === 'PRODUKSI' || actCat === 'SUPPORT' || actCat === 'HAULING' || actCat === 'INFRASTRUKTUR') :
        selectedCategory === 'STANDBY' ? actCat.includes('STANDBY') :
        selectedCategory === 'BREAKDOWN' ? actCat.includes('BREAKDOWN') : true;

      const actUnit = (act.jenis_unit || act.jenisUnit || '').trim().toUpperCase();
      // Standby dan Breakdown berlaku umum atau sesuai jenis unit
      const isUniversal = actUnit === 'ALL' || actUnit === 'SEMUA' || !actUnit || actCat === 'STANDBY' || actCat === 'BREAKDOWN';
      const matchesUnit = isUniversal ||
        actUnit === unitType || actUnit === unitModel ||
        (unitType && (unitType.includes(actUnit) || actUnit.includes(unitType))) ||
        (unitModel && (unitModel.includes(actUnit) || actUnit.includes(unitModel))) ||
        (unitType.includes('EXCAVATOR') && actUnit.includes('EXCAVATOR')) ||
        (unitType.includes('GRADER') && actUnit.includes('GRADER')) ||
        (unitType.includes('COMPACTOR') && actUnit.includes('COMPACTOR')) ||
        ((unitType.includes('TRUCK') || unitType.includes('DT')) && (actUnit.includes('TRUCK') || actUnit.includes('DT')));

      const q = searchQuery.trim().toLowerCase();
      const matchesSearch = !q ||
        (act.nama_aktivitas || act.activityName || '').toLowerCase().includes(q) ||
        (act.kode_sap || act.sapCode || '').toLowerCase().includes(q);

      return matchesCategory && matchesUnit && matchesSearch;
    }).sort((a, b) => {
      const getPriority = (item) => {
        const c = (item.kategori_status || item.kategori || '').toUpperCase();
        if (c.includes('OPERASI') || c === 'PRODUKSI') return 1;
        if (c.includes('STANDBY')) return 2;
        if (c.includes('BREAKDOWN')) return 3;
        return 4;
      };
      return getPriority(a) - getPriority(b);
    });
  }, [allActivities, selectedUnit, selectedCategory, searchQuery]);

  if (!isOpen) return null;

  const unitLabel = selectedUnit
    ? `${selectedUnit.kode_unit || selectedUnit.unitCode} (${selectedUnit.brand_model || selectedUnit.jenis_unit})`
    : 'Semua Unit';

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full flex flex-col overflow-hidden">
        {/* Header Dinamis */}
        <div className="p-6 border-b border-gray-100 flex items-center justify-between">
          <div>
            <h3 className="text-xl font-bold text-gray-900">Pilih Aktivitas Kerja</h3>
            <p className="text-sm font-semibold text-emerald-600 mt-1">
              Sesuai Unit {unitLabel} • {filteredActivities.length} Aktivitas
            </p>
          </div>
          <button onClick={onClose} className="p-2 text-gray-400 hover:text-gray-600 rounded-lg">✕</button>
        </div>

        {/* Search Bar & Category Tabs */}
        <div className="p-6 pb-2 space-y-4">
          <input
            type="text"
            placeholder="Cari nama aktivitas atau kode SAP..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
          />

          {/* Quick Filter Category Tabs */}
          <div className="flex gap-2">
            {[
              { id: 'SEMUA', label: 'Semua' },
              { id: 'OPERASI', label: 'Operasi' },
              { id: 'STANDBY', label: 'Standby' },
              { id: 'BREAKDOWN', label: 'Breakdown' }
            ].map(tab => (
              <button
                key={tab.id}
                onClick={() => setSelectedCategory(tab.id)}
                className={`px-4 py-1.5 rounded-full text-xs font-semibold transition ${
                  selectedCategory === tab.id
                    ? tab.id === 'OPERASI' ? 'bg-emerald-100 text-emerald-800 border border-emerald-500'
                    : tab.id === 'STANDBY' ? 'bg-amber-100 text-amber-800 border border-amber-500'
                    : tab.id === 'BREAKDOWN' ? 'bg-red-100 text-red-800 border border-red-500'
                    : 'bg-emerald-600 text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* List Aktivitas */}
        <div className="p-6 pt-2 overflow-y-auto max-h-96 space-y-2">
          {filteredActivities.length === 0 ? (
            <div className="text-center py-8 text-gray-500 text-sm">
              Tidak ada aktivitas yang sesuai kriteria.
            </div>
          ) : (
            filteredActivities.map(act => {
              const sap = act.kode_sap || act.sapCode;
              const name = act.nama_aktivitas || act.activityName;
              const sat = act.satuan || act.unitOfMeasure || '-';
              const rawKat = (act.kategori_status || act.kategori || 'OPERASI').toUpperCase();
              const badgeStyle = rawKat.includes('STANDBY')
                ? 'bg-amber-50 text-amber-700 border-amber-200'
                : rawKat.includes('BREAKDOWN')
                ? 'bg-red-50 text-red-700 border-red-200'
                : 'bg-emerald-50 text-emerald-700 border-emerald-200';

              return (
                <div
                  key={sap}
                  onClick={() => {
                    onSelectActivity(act);
                    onClose();
                  }}
                  className="flex items-center justify-between p-3.5 rounded-xl border border-gray-200 hover:border-emerald-500 hover:bg-emerald-50/30 cursor-pointer transition"
                >
                  <div>
                    <div className="font-semibold text-gray-900 text-sm">{name}</div>
                    <div className="flex items-center gap-2 mt-1.5">
                      <span className="font-mono text-xs px-2 py-0.5 bg-gray-100 rounded text-gray-700 font-bold">{sap}</span>
                      <span className={`text-xs px-2 py-0.5 rounded font-bold border ${badgeStyle}`}>{rawKat}</span>
                      <span className="text-xs text-gray-500">Satuan: {sat}</span>
                    </div>
                  </div>
                  <button className="text-xs bg-emerald-600 hover:bg-emerald-700 text-white px-3 py-1.5 rounded-lg font-medium">Pilih</button>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}
