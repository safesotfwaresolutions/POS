import React, { useState } from 'react';
import { Users, UserPlus, Search, Mail, Phone, FileText, CheckCircle2 } from 'lucide-react';
import { INITIAL_MOCK_DATA } from '../services/api';

export default function CustomersBento() {
  const [customers, setCustomers] = useState(INITIAL_MOCK_DATA.customers);
  const [search, setSearch] = useState('');

  const filtered = customers.filter(c => 
    c.name.toLowerCase().includes(search.toLowerCase()) || 
    c.identification.includes(search)
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#111c2d]">Clientes Bento UI</h1>
          <p className="text-xs text-gray-500 font-medium">Directorio de clientes para asociación de ventas y emisión de factura electrónica</p>
        </div>

        <button className="px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold flex items-center gap-2 shadow-md shadow-blue-600/20">
          <UserPlus className="w-4 h-4" /> Registrar Nuevo Cliente
        </button>
      </div>

      <div className="bento-card p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div className="relative w-72">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por cédula, NIT o nombre..."
              className="w-full pl-9 pr-3 py-1.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold focus:outline-none focus:border-blue-600"
            />
          </div>
          <span className="text-xs font-bold text-gray-400">{filtered.length} clientes registrados</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map(c => (
            <div key={c.id} className="p-4 bg-gray-50/70 rounded-xl border border-gray-200 space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-[10px] font-mono font-bold text-gray-400">CC/NIT: {c.identification}</span>
                <span className={`text-[10px] px-2 py-0.5 rounded-full font-extrabold ${
                  c.type === 'Consumidor Final' ? 'bg-gray-200 text-gray-700' : 'bg-emerald-100 text-emerald-800'
                }`}>
                  {c.type}
                </span>
              </div>

              <div>
                <h3 className="font-extrabold text-sm text-[#111c2d]">{c.name}</h3>
                <div className="mt-2 space-y-1 text-xs text-gray-500 font-medium">
                  <p className="flex items-center gap-1.5"><Mail className="w-3.5 h-3.5 text-gray-400" /> {c.email}</p>
                  <p className="flex items-center gap-1.5"><Phone className="w-3.5 h-3.5 text-gray-400" /> {c.phone}</p>
                </div>
              </div>

              <div className="pt-2 border-t border-gray-200 flex justify-between items-center text-xs">
                <span className="text-emerald-600 font-bold flex items-center gap-1">
                  <CheckCircle2 className="w-3.5 h-3.5" /> Habilitado DIAN
                </span>
                <button className="text-blue-600 font-extrabold hover:underline">Ver Historial</button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
