import React, { useState, useEffect } from 'react';
import { Save, Store, Key, Printer, AlertCircle } from 'lucide-react';
import { getSettingsApi, updateSettingsApi } from '../services/settingsApi';

export default function SettingsBento() {
  const [saved, setSaved] = useState(false);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [config, setConfig] = useState({
    businessName: 'Mi Pequeño Negocio POS',
    taxId: '900.123.456-7',
    address: 'Calle 10 # 45-12, Medellín, Colombia',
    phone: '300 123 4567',
    email: 'contacto@negocio.com'
  });

  useEffect(() => {
    async function loadSettings() {
      setLoading(true);
      try {
        const data = await getSettingsApi();
        if (data && data.businessName) {
          setConfig({
            businessName: data.businessName || '',
            taxId: data.taxId || '',
            address: data.address || '',
            phone: data.phone || '',
            email: data.email || ''
          });
        }
      } catch (e) {
        console.warn('Cargando valores por defecto de configuración:', e);
      } finally {
        setLoading(false);
      }
    }
    loadSettings();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    try {
      await updateSettingsApi(config);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      setErrorMsg(err.message || 'Error guardando la configuración. Intenta nuevamente.');
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-[#191c1e] dark:text-white">Configuración Bento System</h1>
          <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">Parámetros generales de tu negocio</p>
        </div>

        {saved && (
          <span className="px-3.5 py-1.5 bg-emerald-100 dark:bg-[#12b76a]/20 text-[#006d3c] dark:text-[#12b76a] rounded-2xl text-xs font-extrabold border border-emerald-300 dark:border-[#12b76a]/30 animate-in fade-in">
            ¡Configuración guardada!
          </span>
        )}
      </div>

      {errorMsg && (
        <div className="p-3 bg-red-50 dark:bg-red-950/30 text-red-600 dark:text-red-400 border border-red-200 dark:border-red-800/40 rounded-2xl text-xs font-bold flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Store Data */}
        <div className="bento-card p-6 space-y-4">
          <h3 className="font-extrabold text-sm text-[#191c1e] dark:text-white flex items-center gap-2">
            <Store className="w-4 h-4 text-[#006d3c] dark:text-[#12b76a]" /> Datos Principales de la Tienda
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div>
              <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Nombre Comercial:</label>
              <input
                type="text"
                value={config.businessName}
                onChange={e => setConfig({ ...config, businessName: e.target.value })}
                className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-semibold text-[#191c1e] dark:text-white"
                required
              />
            </div>
            <div>
              <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">NIT / Cédula Fiscal:</label>
              <input
                type="text"
                value={config.taxId}
                onChange={e => setConfig({ ...config, taxId: e.target.value })}
                className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-mono font-bold text-[#191c1e] dark:text-white"
                required
              />
            </div>
            <div>
              <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Dirección Física:</label>
              <input
                type="text"
                value={config.address}
                onChange={e => setConfig({ ...config, address: e.target.value })}
                className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-semibold text-[#191c1e] dark:text-white"
                required
              />
            </div>
            <div>
              <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Teléfono:</label>
              <input
                type="text"
                value={config.phone}
                onChange={e => setConfig({ ...config, phone: e.target.value })}
                className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-semibold text-[#191c1e] dark:text-white"
                required
              />
            </div>
          </div>
        </div>

        {/* Factus API Credentials */}
        <div className="bento-card p-6 space-y-4">
          <h3 className="font-extrabold text-sm text-[#191c1e] dark:text-white flex items-center gap-2">
            <Key className="w-4 h-4 text-[#12b76a]" /> Facturación Electrónica Factus DIAN
          </h3>

          <div className="p-4 bg-emerald-50/50 dark:bg-[#12b76a]/10 rounded-2xl border border-emerald-100 dark:border-[#12b76a]/30 text-xs space-y-1 text-[#006d3c] dark:text-[#12b76a]">
            <p className="font-extrabold">Configuración activa del proveedor de facturación electrónica:</p>
            <p className="text-gray-600 dark:text-gray-300 font-medium">• URL Factus: <code className="font-mono bg-white dark:bg-[#0b1411] px-1.5 py-0.5 rounded border border-gray-200 dark:border-gray-700">https://api-sandbox.factus.com.co</code></p>
            <p className="text-gray-600 dark:text-gray-300 font-medium">• Rango DIAN ID: <code className="font-mono bg-white dark:bg-[#0b1411] px-1.5 py-0.5 rounded border border-gray-200 dark:border-gray-700">8</code></p>
          </div>
        </div>

        <button
          type="submit"
          className="w-full py-3.5 bg-[#006d3c] hover:bg-[#00522c] text-white font-extrabold text-xs rounded-2xl flex items-center justify-center gap-2 shadow-lg shadow-[#006d3c]/30 cursor-pointer"
        >
          <Save className="w-4 h-4" /> Guardar en Base de Datos
        </button>
      </form>
    </div>
  );
}
