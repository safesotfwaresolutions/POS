import React, { useState, useEffect } from 'react';
import {
  Package,
  AlertTriangle,
  ArrowUpRight,
  Search,
  Truck,
  CheckCircle2,
  RefreshCw
} from 'lucide-react';
import { getProductsApi, createPurchaseApi } from '../services/api';

export default function InventoryBento() {
  const [productsList, setProductsList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showPurchaseModal, setShowPurchaseModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [increaseAmount, setIncreaseAmount] = useState('');

  const loadInventory = async () => {
    setLoading(true);
    try {
      const data = await getProductsApi();
      setProductsList(Array.isArray(data) ? data : []);
    } catch (e) {
      console.warn('Error cargando inventario:', e);
      setProductsList([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInventory();
  }, []);

  const lowStockCount = productsList.filter(p => (p.quantityAvailable ?? p.stock ?? 0) <= (p.minStock || 5)).length;

  const handleStockIncrease = async (e) => {
    e.preventDefault();
    const qty = parseInt(increaseAmount);
    if (!selectedProduct || isNaN(qty) || qty <= 0) return;

    try {
      await createPurchaseApi({
        supplierId: 1,
        items: [{
          productId: selectedProduct.id,
          quantity: qty,
          unitCost: selectedProduct.purchasePrice || (selectedProduct.price * 0.7)
        }]
      });
      setShowPurchaseModal(false);
      setSelectedProduct(null);
      setIncreaseAmount('');
      await loadInventory();
    } catch (e) {
      alert('Error registrando compra: ' + e.message);
    }
  };

  const filtered = productsList.filter(p =>
    (p.name && p.name.toLowerCase().includes(search.toLowerCase())) ||
    (p.barcode && p.barcode.includes(search)) ||
    (p.internalCode && p.internalCode.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      {/* Header banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#191c1e] dark:text-white">Inventario Bento Premium</h1>
          <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">Kardex real y reabastecimiento directo de tu inventario</p>
        </div>

        <button
          onClick={() => {
            if (productsList.length > 0) {
              setSelectedProduct(productsList[0]);
              setShowPurchaseModal(true);
            } else {
              alert('Debes crear primero un producto en el catálogo.');
            }
          }}
          className="px-4 py-2.5 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-2xl text-xs font-bold flex items-center gap-2 shadow-md shadow-[#006d3c]/20 cursor-pointer"
        >
          <Truck className="w-4 h-4" /> Registrar Compra a Proveedor (+Stock)
        </button>
      </div>

      {/* Bento Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bento-card p-6 flex items-center justify-between">
          <div>
            <span className="text-xs font-extrabold uppercase text-gray-400 dark:text-gray-500">Total Referencias</span>
            <h3 className="text-2xl font-black text-[#191c1e] dark:text-white mt-1 tabular-nums">{productsList.length} ítems</h3>
            <span className="text-[11px] text-[#006d3c] dark:text-[#12b76a] font-bold">Monitoreo Real</span>
          </div>
          <div className="p-3 bg-emerald-50 dark:bg-[#12b76a]/10 text-[#006d3c] dark:text-[#12b76a] rounded-2xl border border-emerald-100 dark:border-[#12b76a]/30">
            <Package className="w-6 h-6 text-[#006d3c] dark:text-[#12b76a]" />
          </div>
        </div>

        <div className="bento-card p-6 flex items-center justify-between bg-amber-50/50 dark:bg-amber-500/10 border-amber-200 dark:border-amber-500/30">
          <div>
            <span className="text-xs font-extrabold uppercase text-amber-800 dark:text-amber-400">Bajo Stock Mínimo</span>
            <h3 className="text-2xl font-black text-amber-950 dark:text-amber-300 mt-1 tabular-nums">{lowStockCount} Alertas</h3>
            <span className="text-[11px] text-amber-700 dark:text-amber-400 font-bold">Requieren Recompra</span>
          </div>
          <div className="p-3 bg-amber-500 text-white rounded-2xl shadow-xs">
            <AlertTriangle className="w-6 h-6" />
          </div>
        </div>

        <div className="bento-card p-6 flex items-center justify-between">
          <div>
            <span className="text-xs font-extrabold uppercase text-gray-400 dark:text-gray-500">Valorizado Inventario</span>
            <h3 className="text-2xl font-black text-[#191c1e] dark:text-white mt-1 tabular-nums">
              ${productsList.reduce((acc, p) => acc + ((p.salePrice || p.price || 0) * (p.quantityAvailable ?? p.stock ?? 0)), 0).toLocaleString('es-CO')}
            </h3>
            <span className="text-[11px] text-gray-500 dark:text-gray-400 font-bold">Moneda Local (COP)</span>
          </div>
          <div className="p-3 bg-emerald-50 dark:bg-[#12b76a]/10 text-[#006d3c] dark:text-[#12b76a] rounded-2xl border border-emerald-100 dark:border-[#12b76a]/30">
            <ArrowUpRight className="w-6 h-6 text-[#006d3c] dark:text-[#12b76a]" />
          </div>
        </div>
      </div>

      {/* Main Table Card */}
      <div className="bento-card p-6 space-y-4">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 text-gray-400 dark:text-gray-500 absolute left-3.5 top-2.5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar producto o código..."
              className="w-full pl-10 pr-3 py-1.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-[#e0e3e6] dark:border-[#1d332c] rounded-2xl text-xs font-semibold text-[#191c1e] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:border-[#006d3c] dark:focus:border-[#12b76a]"
            />
          </div>
          <button onClick={loadInventory} className="text-xs font-bold text-[#006d3c] dark:text-[#12b76a] flex items-center gap-1 hover:underline cursor-pointer">
            <RefreshCw className="w-3.5 h-3.5" /> Actualizar Kardex
          </button>
        </div>

        {loading ? (
          <div className="py-12 text-center text-xs font-bold text-gray-400 dark:text-gray-500">
            Cargando niveles de stock...
          </div>
        ) : filtered.length === 0 ? (
          <div className="py-10 text-center space-y-2 bg-gray-50/50 dark:bg-white/5 rounded-2xl border border-dashed border-gray-200 dark:border-gray-700">
            <p className="text-xs font-bold text-gray-600 dark:text-gray-300">No hay productos en inventario</p>
            <p className="text-[11px] text-gray-400 dark:text-gray-500">Crea productos desde el catálogo para visualizar las existencias en tiempo real</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead>
                <tr className="border-b border-gray-200 dark:border-gray-800 text-gray-400 dark:text-gray-500 uppercase tracking-wider font-extrabold text-[10px]">
                  <th className="py-3 px-3">Código</th>
                  <th className="py-3 px-3">Producto</th>
                  <th className="py-3 px-3 text-right">Precio Venta</th>
                  <th className="py-3 px-3 text-center">Stock Actual</th>
                  <th className="py-3 px-3 text-center">Stock Mínimo</th>
                  <th className="py-3 px-3 text-center">Estado</th>
                  <th className="py-3 px-3 text-center">Acciones</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-800 font-medium">
                {filtered.map((item) => {
                  const stockVal = item.quantityAvailable ?? item.stock ?? 0;
                  const isLow = stockVal <= (item.minStock || 5);
                  return (
                    <tr key={item.id} className="hover:bg-gray-50 dark:hover:bg-white/5 transition-colors">
                      <td className="py-3 px-3 font-mono font-bold text-gray-500 dark:text-gray-400">{item.internalCode}</td>
                      <td className="py-3 px-3 font-bold text-[#191c1e] dark:text-white">{item.name}</td>
                      <td className="py-3 px-3 text-right font-extrabold text-[#191c1e] dark:text-white tabular-nums">
                        ${(item.salePrice || item.price || 0).toLocaleString('es-CO')}
                      </td>
                      <td className="py-3 px-3 text-center font-black text-sm tabular-nums text-[#191c1e] dark:text-white">
                        {stockVal}
                      </td>
                      <td className="py-3 px-3 text-center font-semibold text-gray-400 dark:text-gray-500 tabular-nums">
                        {item.minStock || 5}
                      </td>
                      <td className="py-3 px-3 text-center">
                        <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold ${
                          isLow ? 'bg-amber-100 dark:bg-amber-500/20 text-amber-800 dark:text-amber-400 border border-amber-300 dark:border-amber-500/30' : 'bg-emerald-100 dark:bg-[#12b76a]/20 text-[#006d3c] dark:text-[#12b76a] border border-emerald-300 dark:border-[#12b76a]/30'
                        }`}>
                          {isLow ? 'REABASTECER' : 'ÓPTIMO'}
                        </span>
                      </td>
                      <td className="py-3 px-3 text-center">
                        <button
                          onClick={() => {
                            setSelectedProduct(item);
                            setShowPurchaseModal(true);
                          }}
                          className="px-2.5 py-1 bg-emerald-50 dark:bg-[#12b76a]/10 hover:bg-[#006d3c] hover:text-white text-[#006d3c] dark:text-[#12b76a] rounded-xl text-[11px] font-bold transition-all cursor-pointer"
                        >
                          + Cargar Stock
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Stock Increase Modal */}
      {showPurchaseModal && selectedProduct && (
        <div className="fixed inset-0 bg-navy-900/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <form onSubmit={handleStockIncrease} className="bento-card max-w-sm w-full bg-white dark:bg-[#14231e] p-6 rounded-3xl shadow-2xl space-y-4">
            <h3 className="font-extrabold text-base text-[#191c1e] dark:text-white">Registrar Compra / Carga de Stock</h3>
            <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">Producto: <span className="font-bold text-[#006d3c] dark:text-[#12b76a]">{selectedProduct.name}</span></p>

            <div className="space-y-1">
              <label className="text-xs font-bold text-gray-600 dark:text-gray-300">Cantidad Comprada:</label>
              <input
                type="number"
                value={increaseAmount}
                onChange={(e) => setIncreaseAmount(e.target.value)}
                placeholder="Ej. 10, 25, 50..."
                className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl text-sm font-bold text-[#191c1e] dark:text-white focus:outline-none focus:border-[#006d3c] dark:focus:border-[#12b76a]"
                required
                min="1"
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowPurchaseModal(false)}
                className="w-1/2 py-2.5 bg-gray-100 dark:bg-[#1e293b] hover:bg-gray-200 dark:hover:bg-[#334155] text-gray-700 dark:text-gray-300 rounded-2xl text-xs font-bold"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="w-1/2 py-2.5 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-2xl text-xs font-extrabold"
              >
                Registrar Compra
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
