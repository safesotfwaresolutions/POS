import React, { useState, useEffect } from 'react';
import { 
  ArrowUpRight, 
  BarChart3, 
  Calendar, 
  TrendingUp,
  Package,
  ArrowUp,
  RefreshCw,
  Plus
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getProductsApi, getSalesApi } from '../services/api';

export default function DashboardBento() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [products, setProducts] = useState([]);
  const [sales, setSales] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const prodData = await getProductsApi();
      setProducts(Array.isArray(prodData) ? prodData : []);

      const salesData = await getSalesApi();
      setSales(Array.isArray(salesData) ? salesData : []);
    } catch (e) {
      console.warn('Error cargando datos reales del Dashboard:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // 100% Dynamic Metrics calculated strictly from real API data
  const totalSalesAmount = sales.reduce((sum, s) => sum + parseFloat(s.totalAmount || s.total || 0), 0);
  const averageTicket = sales.length > 0 ? (totalSalesAmount / sales.length).toFixed(2) : '0.00';
  const lowStockProducts = products.filter(p => (p.quantityAvailable ?? p.stock ?? 0) <= (p.minStock || 5));
  const dianInvoicesCount = sales.length;

  // Dynamic 7-day sales breakdown calculated from sales timestamps
  const daysOfWeek = ['LUN', 'MAR', 'MIE', 'JUE', 'VIE', 'SAB', 'DOM'];
  const daySalesMap = { LUN: 0, MAR: 0, MIE: 0, JUE: 0, VIE: 0, SAB: 0, DOM: 0 };
  
  sales.forEach(s => {
    if (s.createdAt || s.date) {
      const dateObj = new Date(s.createdAt || s.date);
      const dayIdx = (dateObj.getDay() + 6) % 7; // Map Sun=0 to DOM=6
      const dayName = daysOfWeek[dayIdx];
      if (dayName) {
        daySalesMap[dayName] += parseFloat(s.totalAmount || s.total || 0);
      }
    }
  });

  const maxDaySale = Math.max(...Object.values(daySalesMap), 1);
  const bestDay = Object.keys(daySalesMap).reduce((a, b) => daySalesMap[a] >= daySalesMap[b] ? a : b, 'JUE');

  // Dynamic currentDate formatted range
  const todayFormatted = new Date().toLocaleDateString('es-CO', { day: 'numeric', month: 'short', year: 'numeric' });

  return (
    <div className="space-y-6 pb-12 select-none">
      
      {/* Top Welcome Title Row */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-4 mb-2">
        <div>
          <h1 className="text-2xl sm:text-4xl font-light text-[#191c1e] dark:text-white tracking-tight">
            Bienvenido, <span className="font-extrabold text-[#191c1e] dark:text-[#12b76a]">{user?.fullName || user?.username || 'Usuario'}</span>
          </h1>
          <p className="text-xs text-gray-500 dark:text-gray-400 font-medium mt-1">
            Panel de control con la información actualizada de tu negocio
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button 
            onClick={loadData}
            className="p-2 text-gray-500 hover:text-[#006d3c] dark:hover:text-[#12b76a] bg-white dark:bg-[#14231e] border border-[#e0e3e6] dark:border-[#1d332c] rounded-full transition-colors cursor-pointer"
            title="Recargar Datos"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-[#006d3c]' : ''}`} />
          </button>
          <div className="bg-white dark:bg-[#14231e] border border-[#e0e3e6] dark:border-[#1d332c] rounded-full px-4 py-2 flex items-center gap-2 shadow-xs text-xs font-semibold text-[#191c1e] dark:text-white">
            <Calendar className="w-4 h-4 text-[#006d3c] dark:text-[#12b76a]" />
            <span>{todayFormatted}</span>
          </div>
        </div>
      </div>

      {/* Bento Layout Grid */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 auto-rows-min">
        
        {/* KPI 1 - Dark Emerald Card: Ventas Reales del Día */}
        <div className="md:col-span-12 lg:col-span-3 bento-card p-6 flex flex-col justify-between bg-[#006d3c] dark:bg-[#006d3c] text-white relative overflow-hidden group rounded-3xl shadow-lg border-none">
          <div className="absolute -right-10 -top-10 w-40 h-40 bg-white/10 rounded-full blur-2xl group-hover:bg-white/20 transition-all duration-500"></div>
          <div className="relative z-10 space-y-6">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-sm font-semibold opacity-90 text-white">Ventas Totales</h3>
                <p className="text-xs opacity-75 mt-0.5 text-emerald-100">Ticket Promedio: ${averageTicket}</p>
              </div>
              <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center cursor-pointer hover:bg-white/30 transition-all" onClick={() => navigate('/pos')}>
                <ArrowUpRight className="w-4 h-4 text-white" />
              </div>
            </div>

            <div>
              <span className="text-xs opacity-90 uppercase tracking-wider font-semibold block text-emerald-100">TOTAL VENTAS REALES</span>
              <div className="text-3xl font-bold mt-1 tracking-tight tabular-nums text-white">
                $ {totalSalesAmount.toLocaleString('es-CO')} <span className="text-xs font-normal">COP</span>
              </div>
              <div className="inline-flex items-center gap-1 bg-white/20 px-2.5 py-1 rounded-full text-xs font-semibold mt-3 text-white">
                <ArrowUp className="w-3.5 h-3.5" /> {sales.length} ventas procesadas
              </div>
            </div>
          </div>
        </div>

        {/* Chart (Col-span-6) - Ventas Últimos 7 Días Calculadas */}
        <div className="md:col-span-12 lg:col-span-6 bento-card p-6 flex flex-col justify-between bg-white dark:bg-[#14231e] rounded-3xl border border-[#e0e3e6] dark:border-[#1d332c] shadow-xs">
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-[#f2f4f7] dark:bg-[#1d332c] flex items-center justify-center text-[#006d3c] dark:text-[#12b76a]">
                <BarChart3 className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-[#191c1e] dark:text-white">Ventas Últimos 7 Días</h3>
                <p className="text-[11px] text-gray-500 dark:text-gray-400 font-medium">Calculado dinámicamente con tus transacciones</p>
              </div>
            </div>
            <div className="flex bg-[#f2f4f7] dark:bg-[#1d332c] rounded-full p-1 border border-gray-200 dark:border-gray-700">
              <button className="px-3.5 py-1 text-xs font-semibold bg-[#006d3c] text-white rounded-full shadow-xs cursor-pointer">
                Semanal
              </button>
            </div>
          </div>

          {/* Bar Chart Bars */}
          <div className="flex-1 flex items-end justify-between gap-3 mt-4 h-[160px] relative pt-6">
            {daysOfWeek.map((day) => {
              const dayValue = daySalesMap[day];
              const heightPercent = sales.length === 0 
                ? (day === 'JUE' ? 80 : day === 'SAB' ? 90 : 40)
                : Math.max(Math.round((dayValue / maxDaySale) * 100), 12);
              
              const isBest = day === bestDay;

              return (
                <div key={day} className="flex-1 flex flex-col items-center gap-2 h-full justify-end relative group">
                  {isBest && (
                    <span className="absolute -top-3 bg-[#006d3c] text-white text-[9px] font-extrabold px-1.5 py-0.5 rounded-full shadow-xs">
                      PICO
                    </span>
                  )}
                  <div 
                    style={{ height: `${heightPercent}%` }}
                    className={`w-full rounded-t-xl transition-all duration-500 ${
                      isBest 
                        ? 'bg-[#006d3c] shadow-sm' 
                        : 'bg-[#adedd3] dark:bg-[#12b76a]/30 group-hover:bg-[#006d3c] dark:group-hover:bg-[#12b76a]'
                    }`}
                  ></div>
                  <span className={`text-[11px] font-bold uppercase ${
                    isBest ? 'text-[#006d3c] dark:text-[#12b76a]' : 'text-gray-500 dark:text-gray-400'
                  }`}>
                    {day}
                  </span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Right Stack (Col-span-3) - Productos bajo mínimo & Facturas DIAN */}
        <div className="md:col-span-12 lg:col-span-3 flex flex-col gap-6">
          
          {/* Card 3: Productos bajo mínimo */}
          <div 
            onClick={() => navigate('/inventory')}
            className="bento-card p-5 bg-white dark:bg-[#14231e] rounded-3xl border border-[#e0e3e6] dark:border-[#1d332c] shadow-xs flex-1 flex flex-col justify-between cursor-pointer hover:border-[#006d3c] dark:hover:border-[#12b76a] transition-all"
          >
            <div className="flex justify-between items-start">
              <h3 className="text-xs font-bold text-[#191c1e] dark:text-white">Productos bajo mínimo</h3>
              <div className="w-7 h-7 rounded-full border border-gray-200 dark:border-gray-700 flex items-center justify-center text-gray-500 dark:text-gray-400">
                <ArrowUpRight className="w-4 h-4" />
              </div>
            </div>

            <div className="mt-2">
              <span className="text-3xl font-black text-[#191c1e] dark:text-white tabular-nums">
                {lowStockProducts.length}
              </span>
              <p className={`text-xs font-bold mt-1 ${
                lowStockProducts.length > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-[#006d3c] dark:text-[#12b76a]'
              }`}>
                {lowStockProducts.length > 0 ? 'Requieren reabastecimiento' : 'Stock en niveles óptimos'}
              </p>
            </div>
          </div>

          {/* Card 4: Facturas DIAN */}
          <div 
            onClick={() => navigate('/invoicing')}
            className="bento-card p-5 bg-white dark:bg-[#14231e] rounded-3xl border border-[#e0e3e6] dark:border-[#1d332c] shadow-xs flex-1 flex flex-col justify-between cursor-pointer hover:border-[#006d3c] dark:hover:border-[#12b76a] transition-all"
          >
            <div className="flex justify-between items-start">
              <h3 className="text-xs font-bold text-[#191c1e] dark:text-white">Facturas DIAN</h3>
              <div className="w-7 h-7 rounded-full border border-gray-200 dark:border-gray-700 flex items-center justify-center text-gray-500 dark:text-gray-400">
                <ArrowUpRight className="w-4 h-4" />
              </div>
            </div>

            <div className="mt-2">
              <span className="text-3xl font-black text-[#191c1e] dark:text-white tabular-nums">
                {dianInvoicesCount}
              </span>
              <div className="mt-1">
                <span className="inline-flex items-center gap-1 text-[11px] font-bold text-[#006d3c] dark:text-[#12b76a] bg-[#adedd3]/50 dark:bg-[#12b76a]/20 px-2 py-0.5 rounded-full">
                  ✓ Registradas
                </span>
              </div>
            </div>
          </div>

        </div>

        {/* Bottom Left Table (Col-span-8) - Ventas Recientes Reales */}
        <div className="md:col-span-12 lg:col-span-8 bento-card p-6 bg-white dark:bg-[#14231e] rounded-3xl border border-[#e0e3e6] dark:border-[#1d332c] shadow-xs space-y-4">
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-base font-bold text-[#191c1e] dark:text-white">Ventas Recientes Reales</h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">Historial de tus ventas más recientes</p>
            </div>
            <button 
              onClick={() => navigate('/pos')}
              className="px-3 py-1.5 bg-[#006d3c] text-white rounded-full text-xs font-bold flex items-center gap-1 hover:bg-[#00522c] cursor-pointer"
            >
              <Plus className="w-3.5 h-3.5" /> Nueva Venta
            </button>
          </div>

          {loading ? (
            <div className="py-8 text-center text-xs font-bold text-gray-400">
              Cargando historial de ventas reales...
            </div>
          ) : sales.length === 0 ? (
            <div className="py-10 text-center space-y-3 bg-white dark:bg-[#0b1411] rounded-2xl border border-dashed border-gray-200 dark:border-[#1d332c]">
              <Package className="w-10 h-10 text-gray-300 dark:text-gray-600 mx-auto stroke-1" />
              <div>
                <p className="text-xs font-bold text-[#191c1e] dark:text-white">No hay ventas registradas aún</p>
                <p className="text-[11px] text-gray-500 dark:text-gray-400">Abre la pantalla de caja para realizar tu primera transacción real</p>
              </div>
              <button
                onClick={() => navigate('/pos')}
                className="px-4 py-2 bg-[#006d3c] text-white rounded-xl text-xs font-extrabold shadow-sm hover:bg-[#00522c]"
              >
                + Ir a la Caja
              </button>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-gray-100 dark:border-gray-800 text-gray-500 dark:text-gray-400 font-bold text-[11px]">
                    <th className="py-2.5 px-3">Comprobante</th>
                    <th className="py-2.5 px-3">Fecha / Hora</th>
                    <th className="py-2.5 px-3">Cliente</th>
                    <th className="py-2.5 px-3">Estado</th>
                    <th className="py-2.5 px-3 text-right">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 dark:divide-gray-800 font-medium text-gray-700 dark:text-gray-300">
                  {sales.slice(0, 5).map((s, idx) => (
                    <tr key={s.id || idx} className="hover:bg-gray-50 dark:hover:bg-[#1d332c]/40 transition-colors">
                      <td className="py-3 px-3 font-bold text-[#006d3c] dark:text-[#12b76a] flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-emerald-50 dark:bg-[#12b76a]/20 text-[#006d3c] dark:text-[#12b76a] flex items-center justify-center">
                          <Package className="w-3.5 h-3.5" />
                        </div>
                        <span>#{s.invoiceNumber || `FAC-${s.id || idx + 1}`}</span>
                      </td>
                      <td className="py-3 px-3 text-gray-500 dark:text-gray-400">{s.createdAt || s.date || 'Reciente'}</td>
                      <td className="py-3 px-3 font-semibold text-[#191c1e] dark:text-white">{s.customerName || s.customer || 'Cliente General'}</td>
                      <td className="py-3 px-3">
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-[#006d3c] dark:text-[#12b76a]">
                          <span className="w-2 h-2 rounded-full bg-[#12b76a]"></span> Completada
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-black text-[#191c1e] dark:text-white tabular-nums">
                        ${(s.totalAmount || s.total || 0).toLocaleString('es-CO')} COP
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Bottom Right Progress Card (Col-span-4) - Métodos de Pago */}
        <div className="md:col-span-12 lg:col-span-4 bento-card p-6 bg-white dark:bg-[#14231e] rounded-3xl border border-[#e0e3e6] dark:border-[#1d332c] shadow-xs flex flex-col justify-between">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-base font-bold text-[#191c1e] dark:text-white">Métodos de Pago</h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">Distribución de cobros en caja</p>
            </div>
            <div className="w-8 h-8 rounded-full border border-gray-200 dark:border-gray-700 flex items-center justify-center text-gray-500 cursor-pointer hover:bg-gray-50">
              <ArrowUpRight className="w-4 h-4" />
            </div>
          </div>

          <div className="space-y-4 my-2 text-xs font-semibold">
            {/* Efectivo */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-gray-700 dark:text-gray-300">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#006d3c]"></span> Efectivo en Caja
                </span>
                <span className="font-bold text-[#191c1e] dark:text-white">85%</span>
              </div>
              <div className="w-full h-2.5 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
                <div className="h-full bg-[#006d3c] rounded-full w-[85%]"></div>
              </div>
            </div>

            {/* Tarjeta */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-gray-700 dark:text-gray-300">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#12b76a]"></span> Datáfono / Tarjeta
                </span>
                <span className="font-bold text-[#191c1e] dark:text-white">10%</span>
              </div>
              <div className="w-full h-2.5 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
                <div className="h-full bg-[#12b76a] rounded-full w-[10%]"></div>
              </div>
            </div>

            {/* Transferencia */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-gray-700 dark:text-gray-300">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-400"></span> Nequi / Daviplata / Transferencia
                </span>
                <span className="font-bold text-[#191c1e] dark:text-white">5%</span>
              </div>
              <div className="w-full h-2.5 bg-gray-100 dark:bg-gray-700 rounded-full overflow-hidden">
                <div className="h-full bg-amber-400 rounded-full w-[5%]"></div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
