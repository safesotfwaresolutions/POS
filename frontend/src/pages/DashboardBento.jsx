import React, { useState, useEffect } from 'react';
import { 
  TrendingUp, 
  ShoppingCart, 
  AlertTriangle, 
  FileCheck2, 
  ArrowUpRight, 
  Barcode, 
  ChevronRight, 
  Sparkles,
  Package,
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
  const averageTicket = sales.length > 0 ? Math.round(totalSalesAmount / sales.length) : 0;
  const lowStockProducts = products.filter(p => (p.quantityAvailable ?? p.stock ?? 0) <= (p.minStock || 5));

  return (
    <div className="space-y-6 pb-12">
      {/* Welcome Banner - Stitch MCP Emerald Theme */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-[#101e19] via-[#00522c] to-[#006d3c] p-6 rounded-3xl text-white shadow-xl">
        <div className="space-y-1">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#12b76a]/20 text-[#12b76a] border border-[#12b76a]/30 text-xs font-extrabold">
            <Sparkles className="w-3.5 h-3.5" /> Punto de Venta • Caja Única #1
          </div>
          <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight text-white">
            Bienvenido, <span className="font-black text-[#12b76a]">{user?.fullName || user?.username || 'Usuario'}</span>
          </h1>
          <p className="text-xs text-emerald-100/80 font-medium">
            Panel de control operativo en tiempo real • {user?.role || 'ADMINISTRATOR'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button 
            onClick={() => navigate('/pos')}
            className="px-5 py-2.5 bg-[#12b76a] hover:bg-[#0e9656] text-white rounded-2xl font-extrabold text-xs flex items-center gap-2 shadow-lg shadow-[#12b76a]/30 active:scale-95 transition-all cursor-pointer"
          >
            <ShoppingCart className="w-4 h-4" />
            <span>Abrir Pantalla de Caja [F2]</span>
          </button>
        </div>
      </div>

      {/* Bento Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        
        {/* Bento Item 1: Total Ventas del Día */}
        <div className="lg:col-span-2 bento-card p-6 flex flex-col justify-between bg-gradient-to-br from-white via-white to-emerald-50/30 relative overflow-hidden">
          <div className="flex items-start justify-between">
            <div>
              <span className="text-xs font-extrabold uppercase tracking-wider text-gray-400">Total Ventas en Efectivo</span>
              <div className="flex items-baseline gap-3 mt-1">
                <h2 className="text-3xl font-black text-[#191c1e] tabular-nums">
                  ${totalSalesAmount.toLocaleString('es-CO')} <span className="text-sm font-bold text-gray-400">COP</span>
                </h2>
                <span className="inline-flex items-center gap-0.5 text-xs font-extrabold text-[#006d3c] bg-emerald-100/60 px-2 py-0.5 rounded-full border border-emerald-300">
                  <ArrowUpRight className="w-3.5 h-3.5" /> Tiempo real
                </span>
              </div>
            </div>
            <div className="p-3.5 bg-[#006d3c] text-white rounded-2xl shadow-md shadow-[#006d3c]/30">
              <TrendingUp className="w-6 h-6" />
            </div>
          </div>

          <div className="mt-6 space-y-2">
            <div className="flex justify-between text-xs font-bold text-gray-500">
              <span>Rendimiento del día</span>
              <span>{sales.length} transacciones</span>
            </div>
            <div className="w-full h-3 bg-gray-100 rounded-full overflow-hidden p-0.5 border border-gray-200">
              <div className="h-full bg-gradient-to-r from-[#006d3c] to-[#12b76a] rounded-full w-[80%] transition-all duration-500"></div>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between text-xs font-medium text-gray-500">
            <span>Servidor Backend Activo en Puerto 8080</span>
            <span className="font-extrabold text-[#006d3c] hover:underline cursor-pointer flex items-center gap-1" onClick={() => navigate('/pos')}>
              Ir a Caja <ChevronRight className="w-3.5 h-3.5" />
            </span>
          </div>
        </div>

        {/* Bento Item 2: Ticket Promedio */}
        <div className="bento-card p-6 flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <span className="text-xs font-extrabold uppercase tracking-wider text-gray-400">Ticket Promedio</span>
            <div className="p-2.5 bg-emerald-50 text-[#006d3c] rounded-2xl border border-emerald-100">
              <ShoppingCart className="w-5 h-5" />
            </div>
          </div>
          <div className="my-3">
            <h3 className="text-2xl font-black text-[#191c1e] tabular-nums">
              ${averageTicket.toLocaleString('es-CO')}
            </h3>
            <p className="text-xs text-gray-500 mt-0.5 font-medium">Por venta procesada</p>
          </div>
          <span className="text-[11px] text-[#006d3c] font-bold flex items-center gap-1">
            <ArrowUpRight className="w-3 h-3" /> Promedio en base de datos
          </span>
        </div>

        {/* Bento Item 3: Alertas de Inventario */}
        <div className="bento-card p-6 flex flex-col justify-between bg-amber-50/30 border-amber-200/70">
          <div className="flex items-center justify-between">
            <span className="text-xs font-extrabold uppercase tracking-wider text-amber-800">Alerta Inventario</span>
            <div className="p-2.5 bg-amber-500 text-white rounded-2xl shadow-xs">
              <AlertTriangle className="w-5 h-5" />
            </div>
          </div>
          <div className="my-3">
            <h3 className="text-2xl font-black text-amber-950 tabular-nums">
              {lowStockProducts.length} <span className="text-sm font-bold text-amber-800">Productos</span>
            </h3>
            <p className="text-xs text-amber-700 mt-0.5 font-medium">Requieren reabastecimiento</p>
          </div>
          <button 
            onClick={() => navigate('/inventory')}
            className="text-xs font-extrabold text-amber-900 hover:text-amber-950 flex items-center gap-1 cursor-pointer"
          >
            Ver Kardex <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Bento Item 4: Acceso Rápido Atajos de Caja */}
        <div className="lg:col-span-2 bento-card p-6 bg-gradient-to-br from-[#101e19] via-[#00522c] to-[#006d3c] text-white flex flex-col justify-between">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="p-2 bg-emerald-500/20 text-[#12b76a] rounded-xl border border-emerald-400/30">
                <Barcode className="w-5 h-5" />
              </div>
              <h4 className="font-extrabold text-sm text-white">Lector de Código de Barras Escáner</h4>
            </div>
            <span className="px-2.5 py-0.5 rounded-full bg-[#12b76a]/20 text-[#12b76a] text-[10px] font-bold border border-[#12b76a]/30">
              ACTIVO
            </span>
          </div>

          <div className="grid grid-cols-3 gap-2 my-4 text-center">
            <div className="p-2.5 bg-white/5 rounded-2xl border border-white/10">
              <span className="text-[10px] font-mono text-[#12b76a] font-bold block">[F2]</span>
              <span className="text-xs font-semibold text-gray-200">Nueva Venta</span>
            </div>
            <div className="p-2.5 bg-white/5 rounded-2xl border border-white/10">
              <span className="text-[10px] font-mono text-amber-300 font-bold block">[F4]</span>
              <span className="text-xs font-semibold text-gray-200">Cobrar Efectivo</span>
            </div>
            <div className="p-2.5 bg-white/5 rounded-2xl border border-white/10">
              <span className="text-[10px] font-mono text-emerald-300 font-bold block">[F8]</span>
              <span className="text-xs font-semibold text-gray-200">Cliente DIAN</span>
            </div>
          </div>

          <p className="text-[11px] text-emerald-100/80 italic font-medium">
            💡 Escanea cualquier código de barras EAN o código interno para cargar al carrito.
          </p>
        </div>

        {/* Bento Item 5: Facturación Electrónica DIAN Status */}
        <div className="lg:col-span-2 bento-card p-6 flex flex-col justify-between">
          <div className="flex items-start justify-between">
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-extrabold uppercase tracking-wider text-gray-400">Facturación Electrónica DIAN</span>
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-100 text-[#006d3c] text-[10px] font-bold border border-emerald-300">
                  Factus API
                </span>
              </div>
              <h3 className="text-xl font-extrabold text-[#191c1e] mt-1">
                Conexión en Producción / Sandbox
              </h3>
            </div>
            <div className="p-2.5 bg-[#006d3c] text-white rounded-2xl shadow-xs">
              <FileCheck2 className="w-5 h-5" />
            </div>
          </div>

          <div className="p-4 bg-emerald-50/50 rounded-2xl border border-emerald-100 my-4 text-xs space-y-1">
            <span className="font-bold text-[#006d3c]">Estado del Proveedor Tecnológico:</span>
            <p className="text-gray-600 font-medium">Las ventas con cliente seleccionado emitirán la factura electrónica en segundo plano.</p>
          </div>

          <button 
            onClick={() => navigate('/invoicing')}
            className="w-full py-2.5 bg-gray-100 hover:bg-gray-200 text-[#191c1e] font-extrabold text-xs rounded-2xl flex items-center justify-center gap-1.5 transition-colors cursor-pointer"
          >
            <span>Ver Facturas Emitidas</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Bento Item 6: Tabla de Últimas Ventas Reales */}
        <div className="lg:col-span-4 bento-card p-6 space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-extrabold text-base text-[#191c1e]">Últimas Ventas Reales en Base de Datos</h3>
              <p className="text-xs text-gray-500 font-medium">Conectado directamente a Spring Boot REST API</p>
            </div>
            <button 
              onClick={() => navigate('/pos')}
              className="text-xs font-extrabold text-[#006d3c] hover:text-[#00522c] flex items-center gap-1 cursor-pointer"
            >
              Procesar Venta <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>

          {loading ? (
            <div className="py-8 text-center text-xs font-bold text-gray-400">
              Cargando ventas desde el servidor backend...
            </div>
          ) : sales.length === 0 ? (
            <div className="py-10 text-center space-y-3 bg-gray-50/50 rounded-2xl border border-dashed border-gray-200">
              <Package className="w-10 h-10 text-gray-300 mx-auto stroke-1" />
              <div>
                <p className="text-xs font-bold text-gray-600">No hay ventas registradas en la base de datos aún</p>
                <p className="text-[11px] text-gray-400">Realiza tu primera venta desde la pantalla de caja</p>
              </div>
              <button
                onClick={() => navigate('/pos')}
                className="px-4 py-2 bg-[#006d3c] text-white rounded-xl text-xs font-extrabold shadow-sm hover:bg-[#00522c]"
              >
                Registrar Venta
              </button>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-gray-200 text-gray-400 uppercase tracking-wider font-extrabold text-[10px]">
                    <th className="py-3 px-3">Comprobante</th>
                    <th className="py-3 px-3">Fecha / Hora</th>
                    <th className="py-3 px-3">Cliente</th>
                    <th className="py-3 px-3 text-right">Total</th>
                    <th className="py-3 px-3 text-center">Estado</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {sales.map((sale) => (
                    <tr key={sale.id || sale.invoiceNumber} className="hover:bg-gray-50 transition-colors font-medium">
                      <td className="py-3 px-3 font-bold text-[#006d3c]">{sale.invoiceNumber || `FAC-${sale.id}`}</td>
                      <td className="py-3 px-3 text-gray-600">{sale.createdAt || sale.date || 'Reciente'}</td>
                      <td className="py-3 px-3 font-semibold text-[#191c1e]">{sale.customerName || sale.customer || 'Cliente General'}</td>
                      <td className="py-3 px-3 text-right font-extrabold text-[#191c1e] tabular-nums">
                        ${(sale.totalAmount || sale.total || 0).toLocaleString('es-CO')} COP
                      </td>
                      <td className="py-3 px-3 text-center">
                        <span className="px-2.5 py-1 rounded-full text-[10px] font-extrabold bg-emerald-100 text-[#006d3c] border border-emerald-300">
                          COMPLETADA
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
