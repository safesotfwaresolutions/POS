import React, { useState } from 'react';
import { Settings, Save, ShieldCheck, Printer, Store, Key, Database, Cpu } from 'lucide-react';

export default function SettingsBento() {
  const [saved, setSaved] = useState(false);
  const [config, setConfig] = useState({
    storeName: 'Mi Pequeño Negocio POS',
    nit: '900.123.456-7',
    address: 'Calle 10 # 45-12, Medellín, Colombia',
    phone: '300 123 4567',
    cashierName: 'Caja Principal #1',
    factusToken: 'ft_live_9876543210abcdef',
    dianPrefix: 'SETP',
    dianFrom: '1',
    dianTo: '5000',
    printThermal: true,
    paperWidth: '80mm'
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-[#111c2d]">Configuración Bento System</h1>
          <p className="text-xs text-gray-500 font-medium">Parámetros del negocio, caja física y credenciales Factus API DIAN</p>
        </div>

        {saved && (
          <span className="px-3 py-1.5 bg-emerald-100 text-emerald-800 rounded-xl text-xs font-bold border border-emerald-300 animate-in fade-in">
            ¡Configuración guardada!
          </span>
        )}
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Store Data */}
        <div className="bento-card p-6 space-y-4">
          <h3 className="font-extrabold text-sm text-[#111c2d] flex items-center gap-2">
            <Store className="w-4 h-4 text-blue-600" /> Información del Negocio
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div>
              <label className="font-bold text-gray-700 block mb-1">Nombre Comercial:</label>
              <input
                type="text"
                value={config.storeName}
                onChange={e => setConfig({ ...config, storeName: e.target.value })}
                className="w-full p-2.5 bg-gray-50 border border-gray-300 rounded-xl font-semibold"
              />
            </div>
            <div>
              <label className="font-bold text-gray-700 block mb-1">NIT / Cédula del Emisor:</label>
              <input
                type="text"
                value={config.nit}
                onChange={e => setConfig({ ...config, nit: e.target.value })}
                className="w-full p-2.5 bg-gray-50 border border-gray-300 rounded-xl font-mono font-bold"
              />
            </div>
            <div>
              <label className="font-bold text-gray-700 block mb-1">Dirección de la Sucursal:</label>
              <input
                type="text"
                value={config.address}
                onChange={e => setConfig({ ...config, address: e.target.value })}
                className="w-full p-2.5 bg-gray-50 border border-gray-300 rounded-xl font-semibold"
              />
            </div>
            <div>
              <label className="font-bold text-gray-700 block mb-1">Teléfono Móvil / Fijo:</label>
              <input
                type="text"
                value={config.phone}
                onChange={e => setConfig({ ...config, phone: e.target.value })}
                className="w-full p-2.5 bg-gray-50 border border-gray-300 rounded-xl font-semibold"
              />
            </div>
          </div>
        </div>

        {/* Factus API DIAN Credentials */}
        <div className="bento-card p-6 space-y-4">
          <h3 className="font-extrabold text-sm text-[#111c2d] flex items-center gap-2">
            <Key className="w-4 h-4 text-emerald-600" /> Integración Facturación Electrónica Factus API
          </h3>

          <div className="space-y-3 text-xs">
            <div>
              <label className="font-bold text-gray-700 block mb-1">Bearer Token de Producción (Factus):</label>
              <input
                type="password"
                value={config.factusToken}
                onChange={e => setConfig({ ...config, factusToken: e.target.value })}
                className="w-full p-2.5 bg-gray-50 border border-gray-300 rounded-xl font-mono font-bold"
              />
            </div>

            <div className="grid grid-cols-3 gap-3">
              <div>
                <label className="font-bold text-gray-700 block mb-1">Prefijo DIAN:</label>
                <input
                  type="text"
                  value={config.dianPrefix}
                  onChange={e => setConfig({ ...config, dianPrefix: e.target.value })}
                  className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-mono font-bold text-center"
                />
              </div>
              <div>
                <label className="font-bold text-gray-700 block mb-1">Rango Desde:</label>
                <input
                  type="text"
                  value={config.dianFrom}
                  onChange={e => setConfig({ ...config, dianFrom: e.target.value })}
                  className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-mono font-bold text-center"
                />
              </div>
              <div>
                <label className="font-bold text-gray-700 block mb-1">Rango Hasta:</label>
                <input
                  type="text"
                  value={config.dianTo}
                  onChange={e => setConfig({ ...config, dianTo: e.target.value })}
                  className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-mono font-bold text-center"
                />
              </div>
            </div>
          </div>
        </div>

        {/* Printer & Hardware */}
        <div className="bento-card p-6 space-y-4">
          <h3 className="font-extrabold text-sm text-[#111c2d] flex items-center gap-2">
            <Printer className="w-4 h-4 text-indigo-600" /> Impresora Térmica & Periféricos POS
          </h3>

          <div className="flex items-center justify-between text-xs font-bold text-gray-700">
            <span>Impresión Automática de Tiquete al Finalizar Venta:</span>
            <input
              type="checkbox"
              checked={config.printThermal}
              onChange={e => setConfig({ ...config, printThermal: e.target.checked })}
              className="w-4 h-4 text-blue-600 rounded cursor-pointer"
            />
          </div>
        </div>

        <button
          type="submit"
          className="w-full py-3.5 bg-blue-600 hover:bg-blue-700 text-white font-extrabold text-xs rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-blue-600/30"
        >
          <Save className="w-4 h-4" /> Guardar Cambios de Configuración
        </button>
      </form>
    </div>
  );
}
