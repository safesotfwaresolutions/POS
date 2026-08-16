import React, { useState } from 'react';
import { 
  Package, 
  AlertTriangle, 
  PlusCircle, 
  ArrowUpRight, 
  ArrowDownLeft, 
  Search, 
  Filter, 
  Truck,
  CheckCircle,
  RefreshCw
} from 'lucide-react';
import { INITIAL_MOCK_DATA } from '../services/api';

export default function InventoryBento() {
  const [productsList, setProductsList] = useState(INITIAL_MOCK_DATA.products);
  const [search, setSearch] = useState('');
  const [showPurchaseModal, setShowPurchaseModal] = useState(false);
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [increaseAmount, setIncreaseAmount] = useState('');

  const lowStockCount = productsList.filter(p => p.stock <= p.minStock).length;

  const handleStockIncrease = (e) => {
    e.preventDefault();
    const qty = parseInt(increaseAmount);
    if (!selectedProduct || isNaN(qty) || qty <= 0) return;

    setProductsList(prev => prev.map(p => 
      p.id === selectedProduct.id ? { ...p, stock: p.stock + qty } : p
    ));

    setShowPurchaseModal(false);
    setSelectedProduct(null);
    setIncreaseAmount('');
  };

  const filtered = productsList.filter(p => 
    p.name.toLowerCase().includes(search.toLowerCase()) || 
    p.barcode.includes(search) || 
    p.internalCode.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header banner */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#111c2d]">Inventario Bento Premium</h1>
          <p className="text-xs text-gray-500 font-medium">Control de kardex en tiempo real y reabastecimiento de existencias</p>
        </div>

        <button
          onClick={() => {
            setSelectedProduct(productsList[0]);
            setShowPurchaseModal(true);
          }}
          className="px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold flex items-center gap-2 shadow-md shadow-blue-600/20"
        >
          <Truck className="w-4 h-4" /> Registrar Compra a Proveedor (+Stock)
        </button>
      </div>

      {/* Bento Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <div className="bento-card p-5 flex items-center justify-between">
          <div>
            <span className="text-xs font-bold uppercase text-gray-400">Total Referencias</span>
            <h3 className="text-2xl font-black text-[#111c2d] mt-1 tabular-nums">{productsList.length} ítems</h3>
            <span className="text-[11px] text-emerald-600 font-bold">100% Auditados</span>
          </div>
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl">
            <Package className="w-6 h-6" />
          </div>
        </div>

        <div className="bento-card p-5 flex items-center justify-between bg-amber-50/50 border-amber-200">
          <div>
            <span className="text-xs font-bold uppercase text-amber-800">Bajo Stock Mínimo</span>
            <h3 className="text-2xl font-black text-amber-950 mt-1 tabular-nums">{lowStockCount} Alertas</h3>
            <span className="text-[11px] text-amber-700 font-bold">Requieren Recompra</span>
          </div>
          <div className="p-3 bg-amber-500 text-white rounded-xl shadow-xs">
            <AlertTriangle className="w-6 h-6" />
          </div>
        </div>

        <div className="bento-card p-5 flex items-center justify-between">
          <div>
            <span className="text-xs font-bold uppercase text-gray-400">Valorizado Inventario</span>
            <h3 className="text-2xl font-black text-[#111c2d] mt-1 tabular-nums">
              ${productsList.reduce((acc, p) => acc + (p.price * p.stock), 0).toLocaleString('es-CO')}
            </h3>
            <span className="text-[11px] text-gray-500 font-bold">Moneda Local (COP)</span>
          </div>
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl">
            <ArrowUpRight className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Main Table Card */}
      <div className="bento-card p-6 space-y-4">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="relative w-full sm:w-72">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar producto o código..."
              className="w-full pl-9 pr-3 py-1.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold focus:outline-none focus:border-blue-600"
            />
          </div>
          <p className="text-xs text-gray-500 font-semibold">
            Nota: Al vender en la caja, el stock disminuye en tiempo real con bloqueo pesimista.
          </p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-gray-200 text-gray-400 uppercase tracking-wider font-extrabold text-[10px]">
                <th className="py-3 px-3">Código</th>
                <th className="py-3 px-3">Producto</th>
                <th className="py-3 px-3">Categoría</th>
                <th className="py-3 px-3 text-right">Precio Venta</th>
                <th className="py-3 px-3 text-center">Stock Actual</th>
                <th className="py-3 px-3 text-center">Stock Mínimo</th>
                <th className="py-3 px-3 text-center">Estado</th>
                <th className="py-3 px-3 text-center">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 font-medium">
              {filtered.map((item) => {
                const isLow = item.stock <= item.minStock;
                return (
                  <tr key={item.id} className="hover:bg-gray-50 transition-colors">
                    <td className="py-3 px-3 font-mono font-bold text-gray-500">{item.internalCode}</td>
                    <td className="py-3 px-3 font-bold text-[#111c2d]">{item.name}</td>
                    <td className="py-3 px-3 text-gray-600">{item.category}</td>
                    <td className="py-3 px-3 text-right font-extrabold text-[#111c2d] tabular-nums">
                      ${item.price.toLocaleString('es-CO')}
                    </td>
                    <td className="py-3 px-3 text-center font-black text-sm tabular-nums">
                      {item.stock}
                    </td>
                    <td className="py-3 px-3 text-center font-semibold text-gray-400 tabular-nums">
                      {item.minStock}
                    </td>
                    <td className="py-3 px-3 text-center">
                      <span className={`px-2.5 py-1 rounded-full text-[10px] font-extrabold ${
                        isLow ? 'bg-amber-100 text-amber-800 border border-amber-300' : 'bg-emerald-100 text-emerald-800 border border-emerald-300'
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
                        className="px-2.5 py-1 bg-blue-50 hover:bg-blue-600 hover:text-white text-blue-600 rounded-lg text-[11px] font-bold transition-all"
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
      </div>

      {/* Stock Increase Modal */}
      {showPurchaseModal && selectedProduct && (
        <div className="fixed inset-0 bg-navy-900/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <form onSubmit={handleStockIncrease} className="bento-card max-w-sm w-full bg-white p-6 rounded-2xl shadow-2xl space-y-4">
            <h3 className="font-extrabold text-base text-[#111c2d]">Registrar Compra / Incremento</h3>
            <p className="text-xs text-gray-500 font-medium">Producto: <span className="font-bold text-blue-600">{selectedProduct.name}</span></p>

            <div className="space-y-1">
              <label className="text-xs font-bold text-gray-600">Cantidad Comprada a Proveedor:</label>
              <input
                type="number"
                value={increaseAmount}
                onChange={(e) => setIncreaseAmount(e.target.value)}
                placeholder="Ej. 10, 25, 50..."
                className="w-full p-2.5 bg-gray-50 border border-gray-300 rounded-xl text-sm font-bold text-[#111c2d] focus:outline-none focus:border-blue-600"
                required
                min="1"
              />
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowPurchaseModal(false)}
                className="w-1/2 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-bold"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="w-1/2 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-extrabold"
              >
                Aumentar Stock
              </button>
            </div>
          </form>
        </div>
      )}

    </div>
  );
}
