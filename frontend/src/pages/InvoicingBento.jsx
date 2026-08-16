import React, { useState } from 'react';
import { FileCheck2, RefreshCw, AlertCircle, CheckCircle2, ShieldCheck, Download, ExternalLink } from 'lucide-react';
import { INITIAL_MOCK_DATA } from '../services/api';

export default function InvoicingBento() {
  const [invoices, setInvoices] = useState(INITIAL_MOCK_DATA.invoicingDian);

  const handleRetry = (id) => {
    setInvoices(prev => prev.map(inv => {
      if (inv.id === id) {
        return {
          ...inv,
          dianStatus: 'ACEPTADA_DIAN',
          responseCode: '200 - OK (Reintento exitoso)'
        };
      }
      return inv;
    }));
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#111c2d]">Facturación Bento DIAN (Factus Integration)</h1>
          <p className="text-xs text-gray-500 font-medium">Monitoreo de emisión de facturas electrónicas bajo la resolución vigente en Colombia</p>
        </div>

        <div className="flex items-center gap-2">
          <span className="px-3 py-1.5 rounded-xl bg-emerald-100 text-emerald-800 font-bold text-xs border border-emerald-300 flex items-center gap-1.5">
            <ShieldCheck className="w-4 h-4 text-emerald-600" /> Factus API Producción
          </span>
        </div>
      </div>

      {/* Invoicing Table */}
      <div className="bento-card p-6 space-y-4">
        <div className="flex items-center justify-between border-b border-gray-100 pb-3">
          <h3 className="font-extrabold text-sm text-[#111c2d]">Historial de Emisiones de Factura Electrónica</h3>
          <button className="text-xs font-bold text-blue-600 flex items-center gap-1 hover:underline">
            <RefreshCw className="w-3.5 h-3.5" /> Sincronizar con DIAN
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-gray-200 text-gray-400 uppercase tracking-wider font-extrabold text-[10px]">
                <th className="py-3 px-3">Comprobante</th>
                <th className="py-3 px-3">CUFE (Identificador Único)</th>
                <th className="py-3 px-3">Cliente / NIT</th>
                <th className="py-3 px-3">Fecha Emisión</th>
                <th className="py-3 px-3 text-right">Monto Total</th>
                <th className="py-3 px-3 text-center">Respuesta Factus API</th>
                <th className="py-3 px-3 text-center">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 font-medium">
              {invoices.map((inv) => (
                <tr key={inv.id} className="hover:bg-gray-50 transition-colors">
                  <td className="py-3 px-3 font-bold text-blue-600">{inv.id}</td>
                  <td className="py-3 px-3 font-mono text-[11px] text-gray-500">{inv.uuid}</td>
                  <td className="py-3 px-3">
                    <p className="font-bold text-[#111c2d]">{inv.customer}</p>
                    <span className="text-[10px] text-gray-400 font-mono">NIT/CC: {inv.nit}</span>
                  </td>
                  <td className="py-3 px-3 text-gray-600">{inv.date}</td>
                  <td className="py-3 px-3 text-right font-extrabold text-[#111c2d] tabular-nums">
                    ${inv.total.toLocaleString('es-CO')}
                  </td>
                  <td className="py-3 px-3 text-center">
                    <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold ${
                      inv.dianStatus === 'ACEPTADA_DIAN' 
                        ? 'bg-emerald-100 text-emerald-800 border border-emerald-300'
                        : 'bg-amber-100 text-amber-800 border border-amber-300'
                    }`}>
                      {inv.responseCode}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-center">
                    {inv.dianStatus === 'PENDIENTE_REINTENTO' ? (
                      <button
                        onClick={() => handleRetry(inv.id)}
                        className="px-2.5 py-1 bg-amber-500 hover:bg-amber-600 text-white rounded-lg text-[10px] font-bold shadow-xs"
                      >
                        Reintentar Emisión
                      </button>
                    ) : (
                      <button className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg">
                        <Download className="w-4 h-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
