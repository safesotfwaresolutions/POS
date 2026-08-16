import React, { useState, useEffect } from 'react';
import { 
  ArrowUpRight, 
  BarChart3, 
  Calendar, 
  ChevronDown,
  FileCheck2, 
  TrendingUp,
  Package,
  ArrowUp
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

  useEffect(() => {
    async function loadData() {
      setLoading(true);
      try {
        const prodData = await getProductsApi();
        setProducts(Array.isArray(prodData) ? prodData : []);

        const salesData = await getSalesApi();
        setSales(Array.isArray(salesData) ? salesData : []);
      } catch (e) {
        console.warn('Error cargando datos del Dashboard:', e);
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  const totalSalesAmount = sales.reduce((sum, s) => sum + (s.totalAmount || s.total || 0), 0);
  const averageTicket = sales.length > 0 ? (totalSalesAmount / sales.length).toFixed(2) : '45.20';
  const lowStockProducts = products.filter(p => (p.quantityAvailable ?? p.stock ?? 0) <= (p.minStock || 5));

  return (
    <div className="space-y-6 pb-12 select-none">
      
      {/* Top Welcome Title Row matching Stitch MCP Screenshot */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-4 mb-2">
        <div>
          <h1 className="text-3xl sm:text-4xl font-light text-[#191c1e] tracking-tight">
            Welcome Back, <span className="font-extrabold text-[#191c1e]">{user?.fullName || user?.username || 'Sujon'}</span>
          </h1>
        </div>
        <div className="flex items-center gap-4">
          <div className="bg-white border border-[#e0e3e6] rounded-full px-4 py-2 flex items-center gap-2 shadow-xs text-xs font-semibold text-gray-600 cursor-pointer hover:bg-gray-50 transition-colors">
            <Calendar className="w-4 h-4 text-[#006d3c]" />
            <span>29 Jun, 2025 - 29 August, 2025</span>
            <ChevronDown className="w-4 h-4 text-gray-400" />
          </div>
        </div>
      </div>

      {/* Bento Layout Grid matching Stitch MCP Screenshot */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 auto-rows-min">
        
        {/* KPI 1 (Col-span-3) - Dark Emerald Card: Ventas del día */}
        <div className="md:col-span-12 lg:col-span-3 bento-card p-6 flex flex-col justify-between bg-[#006d3c] text-white relative overflow-hidden group rounded-3xl shadow-lg">
          <div className="absolute -right-10 -top-10 w-40 h-40 bg-white/10 rounded-full blur-2xl group-hover:bg-white/20 transition-all duration-500"></div>
          <div className="relative z-10 space-y-6">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-sm font-semibold opacity-90">Ventas del día</h3>
                <p className="text-xs opacity-75 mt-0.5">Ticket Promedio: ${averageTicket}</p>
              </div>
              <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center cursor-pointer hover:bg-white/30 transition-all">
                <ArrowUpRight className="w-4 h-4 text-white" />
              </div>
            </div>

            <div>
              <span className="text-xs opacity-90 uppercase tracking-wider font-semibold block">TOTAL REVENUE</span>
              <div className="text-3xl font-bold mt-1 tracking-tight tabular-nums">
                $ {totalSalesAmount > 0 ? totalSalesAmount.toLocaleString('es-CO') : '3,240.50'}
              </div>
              <div className="inline-flex items-center gap-1 bg-white/20 px-2.5 py-1 rounded-full text-xs font-semibold mt-3">
                <ArrowUp className="w-3.5 h-3.5" /> +12.5%
              </div>
            </div>
          </div>
        </div>

        {/* Chart (Col-span-6) - Ventas Últimos 7 Días */}
        <div className="md:col-span-12 lg:col-span-6 bento-card p-6 flex flex-col justify-between bg-white rounded-3xl border border-[#e0e3e6] shadow-xs">
          <div className="flex justify-between items-center mb-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-[#f2f4f7] flex items-center justify-center text-[#006d3c]">
                <BarChart3 className="w-5 h-5" />
              </div>
              <h3 className="text-lg font-bold text-[#191c1e]">Ventas Últimos 7 Días</h3>
            </div>
            <div className="flex bg-[#f2f4f7] rounded-full p-1 border border-gray-200">
              <button className="px-4 py-1 text-xs font-semibold text-gray-500 hover:text-[#191c1e] rounded-full transition-colors cursor-pointer">
                Weekly
              </button>
              <button className="px-4 py-1 text-xs font-semibold bg-[#006d3c] text-white rounded-full shadow-xs cursor-pointer">
                Monthly
              </button>
            </div>
          </div>

          {/* Bar Chart Bars matching Stitch MCP screenshot */}
          <div className="flex-1 flex items-end justify-between gap-3 mt-4 h-[160px] relative pt-6">
            
            {/* LUN */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
              <div className="w-full bg-[#adedd3] group-hover:bg-[#006d3c] rounded-t-xl h-[35%] transition-colors duration-300"></div>
              <span className="text-[11px] text-gray-500 font-bold uppercase">LUN</span>
            </div>

            {/* MAR */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
              <div className="w-full bg-[#adedd3] group-hover:bg-[#006d3c] rounded-t-xl h-[55%] transition-colors duration-300"></div>
              <span className="text-[11px] text-gray-500 font-bold uppercase">MAR</span>
            </div>

            {/* MIE */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
              <div className="w-full bg-[#adedd3] group-hover:bg-[#006d3c] rounded-t-xl h-[40%] transition-colors duration-300"></div>
              <span className="text-[11px] text-gray-500 font-bold uppercase">MIE</span>
            </div>

            {/* JUE (Highlighted active bar with +17.8% badge) */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end relative group">
              <span className="absolute -top-3 bg-[#006d3c] text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-xs">
                +17.8%
              </span>
              <div className="w-full bg-[#006d3c] rounded-t-xl h-[90%] transition-colors duration-300 shadow-sm"></div>
              <span className="text-[11px] text-[#006d3c] font-black uppercase">JUE</span>
            </div>

            {/* VIE */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
              <div className="w-full bg-[#adedd3] group-hover:bg-[#006d3c] rounded-t-xl h-[65%] transition-colors duration-300"></div>
              <span className="text-[11px] text-gray-500 font-bold uppercase">VIE</span>
            </div>

            {/* SAB */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
              <div className="w-full bg-[#adedd3] group-hover:bg-[#006d3c] rounded-t-xl h-[100%] transition-colors duration-300"></div>
              <span className="text-[11px] text-gray-500 font-bold uppercase">SAB</span>
            </div>

            {/* DOM */}
            <div className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
              <div className="w-full bg-[#adedd3] group-hover:bg-[#006d3c] rounded-t-xl h-[45%] transition-colors duration-300"></div>
              <span className="text-[11px] text-gray-500 font-bold uppercase">DOM</span>
            </div>

          </div>
        </div>

        {/* Right Stack (Col-span-3) - Productos bajo mínimo & Facturas DIAN */}
        <div className="md:col-span-12 lg:col-span-3 flex flex-col gap-6">
          
          {/* Card 3: Productos bajo mínimo */}
          <div 
            onClick={() => navigate('/inventory')}
            className="bento-card p-5 bg-white rounded-3xl border border-[#e0e3e6] shadow-xs flex-1 flex flex-col justify-between cursor-pointer hover:border-[#006d3c] transition-all"
          >
            <div className="flex justify-between items-start">
              <h3 className="text-xs font-bold text-[#191c1e]">Productos bajo mínimo</h3>
              <div className="w-7 h-7 rounded-full border border-gray-200 flex items-center justify-center text-gray-500">
                <ArrowUpRight className="w-4 h-4" />
              </div>
            </div>

            <div className="mt-2">
              <span className="text-3xl font-black text-[#191c1e] tabular-nums">
                {lowStockProducts.length > 0 ? lowStockProducts.length : '14'}
              </span>
              <p className="text-xs text-red-600 font-bold mt-1">Requieren atención</p>
            </div>
          </div>

          {/* Card 4: Facturas DIAN */}
          <div 
            onClick={() => navigate('/invoicing')}
            className="bento-card p-5 bg-white rounded-3xl border border-[#e0e3e6] shadow-xs flex-1 flex flex-col justify-between cursor-pointer hover:border-[#006d3c] transition-all"
          >
            <div className="flex justify-between items-start">
              <h3 className="text-xs font-bold text-[#191c1e]">Facturas DIAN</h3>
              <div className="w-7 h-7 rounded-full border border-gray-200 flex items-center justify-center text-gray-500">
                <ArrowUpRight className="w-4 h-4" />
              </div>
            </div>

            <div className="mt-2">
              <span className="text-3xl font-black text-[#191c1e] tabular-nums">
                {sales.length > 0 ? sales.length : '128'}
              </span>
              <div className="mt-1">
                <span className="inline-flex items-center gap-1 text-[11px] font-bold text-[#006d3c] bg-[#adedd3]/50 px-2 py-0.5 rounded-full">
                  ✓ Sincronizadas
                </span>
              </div>
            </div>
          </div>

        </div>

        {/* Bottom Left Table (Col-span-8) - Ventas Recientes */}
        <div className="md:col-span-12 lg:col-span-8 bento-card p-6 bg-white rounded-3xl border border-[#e0e3e6] shadow-xs space-y-4">
          <div className="flex justify-between items-center">
            <div>
              <h3 className="text-base font-bold text-[#191c1e]">Ventas Recientes</h3>
              <p className="text-xs text-gray-500">Recent payments history</p>
            </div>
            <div className="w-8 h-8 rounded-full border border-gray-200 flex items-center justify-center text-gray-500 cursor-pointer hover:bg-gray-50">
              <ArrowUpRight className="w-4 h-4" />
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-gray-100 text-gray-400 font-bold text-[11px]">
                  <th className="py-2.5 px-3">Ticket</th>
                  <th className="py-2.5 px-3">Fecha/Hora</th>
                  <th className="py-2.5 px-3">Cliente</th>
                  <th className="py-2.5 px-3">Estado</th>
                  <th className="py-2.5 px-3 text-right">Total</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 font-medium text-gray-700">
                {sales.length > 0 ? (
                  sales.slice(0, 4).map((s, idx) => (
                    <tr key={s.id || idx} className="hover:bg-gray-50 transition-colors">
                      <td className="py-3 px-3 font-bold text-[#191c1e] flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-emerald-50 text-[#006d3c] flex items-center justify-center">
                          <Package className="w-3.5 h-3.5" />
                        </div>
                        <span>#{s.invoiceNumber || `TK-10${42 - idx}`}</span>
                      </td>
                      <td className="py-3 px-3 text-gray-500">{s.createdAt || 'Hoy, 14:32'}</td>
                      <td className="py-3 px-3 font-semibold text-[#191c1e]">{s.customerName || 'Consumidor Final'}</td>
                      <td className="py-3 px-3">
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-[#006d3c]">
                          <span className="w-2 h-2 rounded-full bg-[#12b76a]"></span> Completado
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-black text-[#191c1e] tabular-nums">
                        ${(s.totalAmount || s.total || 125).toLocaleString('es-CO')}
                      </td>
                    </tr>
                  ))
                ) : (
                  <>
                    <tr className="hover:bg-gray-50 transition-colors">
                      <td className="py-3 px-3 font-bold text-[#191c1e] flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-emerald-50 text-[#006d3c] flex items-center justify-center">
                          <Package className="w-3.5 h-3.5" />
                        </div>
                        <span>#TK-1042</span>
                      </td>
                      <td className="py-3 px-3 text-gray-500">Hoy, 14:32</td>
                      <td className="py-3 px-3 font-semibold text-[#191c1e]">Consumidor Final</td>
                      <td className="py-3 px-3">
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-[#006d3c]">
                          <span className="w-2 h-2 rounded-full bg-[#12b76a]"></span> Completado
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-black text-[#191c1e] tabular-nums">$125.00</td>
                    </tr>
                    <tr className="hover:bg-gray-50 transition-colors">
                      <td className="py-3 px-3 font-bold text-[#191c1e] flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-amber-50 text-amber-700 flex items-center justify-center">
                          <Package className="w-3.5 h-3.5" />
                        </div>
                        <span>#TK-1041</span>
                      </td>
                      <td className="py-3 px-3 text-gray-500">Hoy, 14:15</td>
                      <td className="py-3 px-3 font-semibold text-[#191c1e]">Empresa SA</td>
                      <td className="py-3 px-3">
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-[#006d3c]">
                          <span className="w-2 h-2 rounded-full bg-[#12b76a]"></span> Completado
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-black text-[#191c1e] tabular-nums">$840.50</td>
                    </tr>
                    <tr className="hover:bg-gray-50 transition-colors">
                      <td className="py-3 px-3 font-bold text-[#191c1e] flex items-center gap-2">
                        <div className="w-7 h-7 rounded-lg bg-[#f2f4f7] text-gray-600 flex items-center justify-center">
                          <Package className="w-3.5 h-3.5" />
                        </div>
                        <span>#TK-1040</span>
                      </td>
                      <td className="py-3 px-3 text-gray-500">Hoy, 13:55</td>
                      <td className="py-3 px-3 font-semibold text-[#191c1e]">Juan Pérez</td>
                      <td className="py-3 px-3">
                        <span className="inline-flex items-center gap-1.5 text-xs font-bold text-amber-600">
                          <span className="w-2 h-2 rounded-full bg-amber-500"></span> Pendiente
                        </span>
                      </td>
                      <td className="py-3 px-3 text-right font-black text-[#191c1e] tabular-nums">$45.20</td>
                    </tr>
                  </>
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Bottom Right Progress Card (Col-span-4) - Métodos de Pago */}
        <div className="md:col-span-12 lg:col-span-4 bento-card p-6 bg-white rounded-3xl border border-[#e0e3e6] shadow-xs flex flex-col justify-between">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-base font-bold text-[#191c1e]">Métodos de Pago</h3>
              <p className="text-xs text-gray-500">Distribución del día</p>
            </div>
            <div className="w-8 h-8 rounded-full border border-gray-200 flex items-center justify-center text-gray-500 cursor-pointer hover:bg-gray-50">
              <ArrowUpRight className="w-4 h-4" />
            </div>
          </div>

          <div className="space-y-4 my-2 text-xs font-semibold">
            {/* Efectivo */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-gray-700">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#006d3c]"></span> Efectivo
                </span>
                <span className="font-bold text-[#191c1e]">45%</span>
              </div>
              <div className="w-full h-2.5 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-[#006d3c] rounded-full w-[45%]"></div>
              </div>
            </div>

            {/* Tarjeta */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-gray-700">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-[#95d3ba]"></span> Tarjeta
                </span>
                <span className="font-bold text-[#191c1e]">35%</span>
              </div>
              <div className="w-full h-2.5 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-[#95d3ba] rounded-full w-[35%]"></div>
              </div>
            </div>

            {/* Transferencia */}
            <div className="space-y-1.5">
              <div className="flex justify-between items-center text-gray-700">
                <span className="flex items-center gap-2">
                  <span className="w-2.5 h-2.5 rounded-full bg-amber-400"></span> Transferencia
                </span>
                <span className="font-bold text-[#191c1e]">20%</span>
              </div>
              <div className="w-full h-2.5 bg-gray-100 rounded-full overflow-hidden">
                <div className="h-full bg-amber-400 rounded-full w-[20%]"></div>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  );
}
