import React, { useState, useEffect } from 'react';
import { 
  TrendingUp, 
  ShoppingCart, 
  AlertTriangle, 
  FileCheck2, 
  ArrowUpRight, 
  Barcode, 
  ChevronRight, 
  Sparkles 
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getProductsApi, getSalesApi, INITIAL_MOCK_DATA } from '../services/api';

export default function DashboardBento() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [stats, setStats] = useState(INITIAL_MOCK_DATA.stats);
  const [recentSales, setRecentSales] = useState(INITIAL_MOCK_DATA.recentSales);
  const [products, setProducts] = useState(INITIAL_MOCK_DATA.products);

  useEffect(() => {
    async function loadLiveData() {
      try {
        const prodData = await getProductsApi();
        if (Array.isArray(prodData) && prodData.length > 0) {
          setProducts(prodData);
        }
        const salesData = await getSalesApi();
        if (Array.isArray(salesData) && salesData.length > 0) {
          setRecentSales(salesData);
        }
      } catch (e) {
        console.warn('Usando datos de respaldo para el Dashboard:', e);
      }
    }
    loadLiveData();
  }, []);

  const lowStockCount = products.filter(p => p.stock <= (p.minStock || 5)).length;

  return (
    <div className="space-y-6 pb-12">
      {/* Welcome Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-[#101828] via-[#1a2942] to-[#004ac6] p-6 rounded-2xl text-white shadow-xl">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-blue-500/30 text-blue-200 border border-blue-400/30 text-xs font-bold">
            <Sparkles className="w-3.5 h-3.5" /> Punto de Venta • Caja Única #1
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight">
            ¡Hola, {user?.fullName || user?.username || 'Usuario'}! 👋
          </h1>
          <p className="text-xs text-blue-100/80 font-medium">
            Resumen operativo en tiempo real • {user?.role || 'ADMINISTRATOR'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button 
            onClick={() => navigate('/pos')}
            className="px-5 py-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-xl font-extrabold text-xs flex items-center gap-2 shadow-lg shadow-blue-600/40 active:scale-95 transition-all"
          >
            <ShoppingCart className="w-4 h-4" />
            <span>Abrir Pantalla de Caja [F2]</span>
          </button>
        </div>
      </div>

      {/* Bento Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        
        {/* Bento Item 1: Total Ventas Hoy */}
        <div className="lg:col-span-2 bento-card p-6 flex flex-col justify-between bg-gradient-to-br from-white via-white to-blue-50/40 relative overflow-hidden">
          <div className="flex items-start justify-between">
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-gray-400">Total Ventas en Efectivo Hoy</span>
              <div className="flex items-baseline gap-3 mt-1">
                <h2 className="text-3xl font-black text-[#111c2d] tabular-nums">
                  ${stats.todaySales.toLocaleString('es-CO')} <span className="text-sm font-bold text-gray-400">COP</span>
                </h2>
                <span className="inline-flex items-center gap-0.5 text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                  <ArrowUpRight className="w-3.5 h-3.5" /> +18.4% vs ayer
                </span>
              </div>
            </div>
            <div className="p-3 bg-blue-600 text-white rounded-2xl shadow-md shadow-blue-600/30">
              <TrendingUp className="w-6 h-6" />
            </div>
          </div>

          <div className="mt-6 space-y-2">
            <div className="flex justify-between text-xs font-bold text-gray-500">
              <span>Meta diaria ($2,000,000 COP)</span>
              <span>71% completado</span>
            </div>
            <div className="w-full h-3 bg-gray-100 rounded-full overflow-hidden p-0.5 border border-gray-200">
              <div className="h-full bg-gradient-to-r from-blue-600 to-indigo-500 rounded-full w-[71%] transition-all duration-500"></div>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between text-xs font-medium text-gray-500">
            <span>{recentSales.length} Transacciones procesadas</span>
            <span className="font-bold text-blue-600 hover:underline cursor-pointer flex items-center gap-1" onClick={() => navigate('/pos')}>
              Ir a Caja <ChevronRight className="w-3.5 h-3.5" />
            </span>
          </div>
        </div>

        {/* Bento Item 2: Ticket Promedio */}
        <div className="bento-card p-5 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-gray-400">Ticket Promedio</span>
            <div className="p-2.5 bg-indigo-50 text-indigo-600 rounded-xl">
              <ShoppingCart className="w-5 h-5" />
            </div>
          </div>
          <div className="my-3">
            <h3 className="text-2xl font-black text-[#111c2d] tabular-nums">
              ${stats.averageTicket.toLocaleString('es-CO')}
            </h3>
            <p className="text-xs text-gray-500 mt-0.5">Por transacción realizada</p>
          </div>
          <span className="text-[11px] text-emerald-600 font-bold flex items-center gap-1">
            <ArrowUpRight className="w-3 h-3" /> +4.2% esta semana
          </span>
        </div>

        {/* Bento Item 3: Alerta de Inventario */}
        <div className="bento-card p-5 flex flex-col justify-between bg-amber-50/30 border-amber-200/70">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-amber-800">Alerta de Inventario</span>
            <div className="p-2.5 bg-amber-500 text-white rounded-xl shadow-xs">
              <AlertTriangle className="w-5 h-5" />
            </div>
          </div>
          <div className="my-3">
            <h3 className="text-2xl font-black text-amber-950 tabular-nums">
              {lowStockCount} <span className="text-sm font-bold text-amber-800">Productos</span>
            </h3>
            <p className="text-xs text-amber-700 mt-0.5 font-medium">Bajo stock mínimo requerido</p>
          </div>
          <button 
            onClick={() => navigate('/inventory')}
            className="text-xs font-extrabold text-amber-900 hover:text-amber-950 flex items-center gap-1"
          >
            Revisar Kardex <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Bento Item 4: Facturación DIAN */}
        <div className="lg:col-span-2 bento-card p-6 flex flex-col justify-between">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold uppercase tracking-wider text-gray-400">Facturación Electrónica DIAN</span>
                <span className="px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-800 text-[10px] font-bold border border-emerald-300">
                  Factus API
                </span>
              </div>
              <h3 className="text-xl font-extrabold text-[#111c2d] mt-1">
                94.1% Transmitidas Exitosamente
              </h3>
            </div>
            <div className="p-2.5 bg-emerald-600 text-white rounded-xl">
              <FileCheck2 className="w-5 h-5" />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3 my-4">
            <div className="p-3 bg-gray-50 rounded-xl border border-gray-100">
              <span className="text-[11px] font-semibold text-gray-500">Aceptadas DIAN</span>
              <p className="text-lg font-black text-emerald-600">32 facturas</p>
            </div>
            <div className="p-3 bg-amber-50 rounded-xl border border-amber-100">
              <span className="text-[11px] font-semibold text-amber-700">Pendientes de Reintento</span>
              <p className="text-lg font-black text-amber-600">2 facturas</p>
            </div>
          </div>

          <button 
            onClick={() => navigate('/invoicing')}
            className="w-full py-2 bg-gray-100 hover:bg-gray-200 text-[#111c2d] font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-colors"
          >
            <span>Ver Monitor de Facturas DIAN</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Bento Item 5: Acceso Rápido Atajos de Caja */}
        <div className="lg:col-span-2 bento-card p-6 bg-gradient-to-br from-blue-900 via-[#101828] to-gray-900 text-white flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-2 bg-blue-500/20 text-blue-300 rounded-lg border border-blue-400/30">
                <Barcode className="w-5 h-5" />
              </div>
              <h4 className="font-extrabold text-sm text-white">Lector de Código de Barras Escáner</h4>
            </div>
            <span className="px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-300 text-[10px] font-bold border border-emerald-500/30">
              ACTIVO
            </span>
          </div>

          <div className="grid grid-cols-3 gap-2 my-4 text-center">
            <div className="p-2.5 bg-white/5 rounded-xl border border-white/10">
              <span className="text-[10px] font-mono text-blue-300 font-bold block">[F2]</span>
              <span className="text-xs font-semibold text-gray-300">Nueva Venta</span>
            </div>
            <div className="p-2.5 bg-white/5 rounded-xl border border-white/10">
              <span className="text-[10px] font-mono text-amber-300 font-bold block">[F4]</span>
              <span className="text-xs font-semibold text-gray-300">Cobrar Efectivo</span>
            </div>
            <div className="p-2.5 bg-white/5 rounded-xl border border-white/10">
              <span className="text-[10px] font-mono text-emerald-300 font-bold block">[F8]</span>
              <span className="text-xs font-semibold text-gray-300">Cliente DIAN</span>
            </div>
          </div>

          <p className="text-[11px] text-gray-400 italic">
            💡 El lector USB o inalámbrico agrega automáticamente el producto al carrito sin perder el foco.
          </p>
        </div>

        {/* Bento Item 6: Tabla de Últimas Ventas */}
        <div className="lg:col-span-4 bento-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-extrabold text-base text-[#111c2d]">Últimas Ventas Registradas en Caja</h3>
              <p className="text-xs text-gray-500">Monitoreo de transacciones e historial inmediato</p>
            </div>
            <button 
              onClick={() => navigate('/pos')}
              className="text-xs font-extrabold text-blue-600 hover:text-blue-700 flex items-center gap-1"
            >
              Procesar Nueva Venta <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-gray-200 text-gray-400 uppercase tracking-wider font-extrabold text-[10px]">
                  <th className="py-3 px-3">Comprobante</th>
                  <th className="py-3 px-3">Fecha / Hora</th>
                  <th className="py-3 px-3">Cliente</th>
                  <th className="py-3 px-3 text-center">Ítems</th>
                  <th className="py-3 px-3 text-right">Total</th>
                  <th className="py-3 px-3 text-center">Factura DIAN</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {recentSales.map((sale) => (
                  <tr key={sale.id} className="hover:bg-gray-50 transition-colors">
                    <td className="py-3 px-3 font-bold text-blue-600">{sale.id}</td>
                    <td className="py-3 px-3 text-gray-600">{sale.date || sale.createdAt}</td>
                    <td className="py-3 px-3 font-semibold text-[#111c2d]">{sale.customer || 'Cliente General'}</td>
                    <td className="py-3 px-3 text-center font-bold">{sale.itemsCount || 1}</td>
                    <td className="py-3 px-3 text-right font-extrabold text-[#111c2d] tabular-nums">
                      ${(sale.total || sale.totalAmount || 0).toLocaleString('es-CO')} COP
                    </td>
                    <td className="py-3 px-3 text-center">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                        sale.dianStatus === 'APROBADA' || sale.dianStatus === 'APROBADA_DIAN'
                          ? 'bg-emerald-100 text-emerald-800 border border-emerald-300'
                          : 'bg-gray-100 text-gray-600 border border-gray-200'
                      }`}>
                        {sale.dianStatus || 'NO_REQUERIDA'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </div>
  );
}
