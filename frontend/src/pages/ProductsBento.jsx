import React, { useState, useEffect } from 'react';
import {
  Tag,
  Plus,
  Search,
  Edit3,
  Trash2,
  Barcode,
  Package,
  Check,
  AlertCircle
} from 'lucide-react';
import { getProductsApi, createProductApi, deleteProductApi } from '../services/api';
import BarcodeModal from '../components/BarcodeModal';

export default function ProductsBento() {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [barcodeProduct, setBarcodeProduct] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  const [newProduct, setNewProduct] = useState({
    name: '',
    barcode: '',
    internalCode: '',
    category: 'Abarrotes',
    price: '',
    stock: '',
    minStock: 5
  });

  const loadProducts = async () => {
    setLoading(true);
    try {
      const data = await getProductsApi();
      setProducts(Array.isArray(data) ? data : []);
    } catch (e) {
      console.warn('Error cargando productos:', e);
      setProducts([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  const handleAddProduct = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    if (!newProduct.name || !newProduct.price) return;

    try {
      await createProductApi({
        name: newProduct.name,
        barcode: newProduct.barcode || `${Math.floor(1000000000000 + Math.random() * 9000000000000)}`,
        internalCode: newProduct.internalCode || `PROD-00${products.length + 1}`,
        price: parseFloat(newProduct.price),
        stock: parseInt(newProduct.stock) || 0,
        minStock: parseInt(newProduct.minStock) || 5
      });
      setShowAddModal(false);
      setNewProduct({ name: '', barcode: '', internalCode: '', category: 'Abarrotes', price: '', stock: '', minStock: 5 });
      await loadProducts();
    } catch (err) {
      setErrorMsg(err.message || 'Error al guardar el producto. Intenta nuevamente.');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('¿Seguro que deseas eliminar este producto?')) return;
    try {
      await deleteProductApi(id);
      await loadProducts();
    } catch (e) {
      alert('Error eliminando producto: ' + e.message);
    }
  };

  const filtered = products.filter(p =>
    (p.name && p.name.toLowerCase().includes(search.toLowerCase())) ||
    (p.barcode && p.barcode.includes(search)) ||
    (p.internalCode && p.internalCode.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#191c1e] dark:text-white">Catálogo de Productos Bento</h1>
          <p className="text-xs text-gray-500 dark:text-gray-400 font-medium">Gestión completa de productos y códigos de barras</p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="px-4 py-2.5 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-2xl text-xs font-bold flex items-center gap-2 shadow-md shadow-[#006d3c]/20 cursor-pointer"
        >
          <Plus className="w-4 h-4" /> Crear Nuevo Producto
        </button>
      </div>

      <div className="bento-card p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div className="relative w-72">
            <Search className="w-4 h-4 text-gray-400 dark:text-gray-500 absolute left-3.5 top-2.5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Filtrar por nombre o código de barras..."
              className="w-full pl-10 pr-3 py-1.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-[#e0e3e6] dark:border-[#1d332c] rounded-2xl text-xs font-semibold text-[#191c1e] dark:text-white placeholder-gray-400 dark:placeholder-gray-500 focus:outline-none focus:border-[#006d3c] dark:focus:border-[#12b76a]"
            />
          </div>
          <span className="text-xs font-bold text-gray-400 dark:text-gray-500">{filtered.length} productos registrados</span>
        </div>

        {loading ? (
          <div className="py-12 text-center text-xs font-bold text-gray-400 dark:text-gray-500">
            Consultando catálogo de productos...
          </div>
        ) : filtered.length === 0 ? (
          <div className="py-12 text-center space-y-3 bg-gray-50/50 dark:bg-white/5 rounded-2xl border border-dashed border-gray-200 dark:border-gray-700">
            <Package className="w-12 h-12 text-gray-300 dark:text-gray-600 mx-auto stroke-1" />
            <div>
              <p className="text-xs font-bold text-gray-700 dark:text-gray-300">No se encontraron productos</p>
              <p className="text-[11px] text-gray-400 dark:text-gray-500">Crea tu primer producto para empezar a registrar ventas</p>
            </div>
            <button
              onClick={() => setShowAddModal(true)}
              className="px-4 py-2 bg-[#006d3c] text-white rounded-xl text-xs font-extrabold shadow-sm hover:bg-[#00522c]"
            >
              + Registrar Primer Producto
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filtered.map(p => (
              <div key={p.id} className="p-5 bg-gray-50/60 dark:bg-white/5 rounded-2xl border border-gray-200 dark:border-gray-700 space-y-3 flex flex-col justify-between hover:bg-white dark:hover:bg-[#14231e] hover:border-[#12b76a] transition-all shadow-xs">
                <div>
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-100 dark:bg-[#12b76a]/20 text-[#006d3c] dark:text-[#12b76a] font-extrabold">
                      {p.categoryName || 'General'}
                    </span>
                    <span className="text-[10px] font-mono text-gray-400 dark:text-gray-500 font-bold">{p.internalCode}</span>
                  </div>
                  <h3 className="font-extrabold text-sm text-[#191c1e] dark:text-white mt-2">{p.name}</h3>
                  <p className="text-[11px] text-gray-500 dark:text-gray-400 font-mono mt-0.5 flex items-center gap-1">
                    <Barcode className="w-3.5 h-3.5 text-gray-400 dark:text-gray-500" /> {p.barcode || 'Sin código'}
                  </p>
                </div>

                <div className="pt-3 border-t border-gray-200 dark:border-gray-700 flex items-center justify-between">
                  <div>
                    <span className="text-[10px] text-gray-400 dark:text-gray-500 block font-semibold">Precio de venta</span>
                    <span className="text-base font-black text-[#191c1e] dark:text-white tabular-nums">
                      ${(p.salePrice || p.price || 0).toLocaleString('es-CO')} COP
                    </span>
                  </div>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setBarcodeProduct(p)}
                      title="Ver código de barras"
                      className="p-1.5 text-gray-400 dark:text-gray-500 hover:text-[#006d3c] dark:hover:text-[#12b76a] rounded-lg hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer"
                    >
                      <Barcode className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(p.id)}
                      title="Eliminar producto"
                      className="p-1.5 text-gray-400 dark:text-gray-500 hover:text-red-600 dark:hover:text-red-400 rounded-lg hover:bg-gray-100 dark:hover:bg-white/10 cursor-pointer"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Add Product Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-navy-900/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <form onSubmit={handleAddProduct} className="bento-card max-w-md w-full bg-white dark:bg-[#14231e] p-6 rounded-3xl shadow-2xl space-y-4">
            <h3 className="font-extrabold text-base text-[#191c1e] dark:text-white">Crear Nuevo Producto</h3>

            {errorMsg && (
              <div className="p-3 bg-red-50 dark:bg-red-950/30 text-red-600 dark:text-red-400 border border-red-200 dark:border-red-800/40 rounded-xl text-xs font-bold flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}

            <div className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Nombre Comercial del Producto:</label>
                <input
                  type="text"
                  value={newProduct.name}
                  onChange={e => setNewProduct({ ...newProduct, name: e.target.value })}
                  placeholder="Ej. Arroz 1kg"
                  className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-semibold text-[#191c1e] dark:text-white"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Código de Barras:</label>
                  <input
                    type="text"
                    value={newProduct.barcode}
                    onChange={e => setNewProduct({ ...newProduct, barcode: e.target.value })}
                    placeholder="770..."
                    className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-mono text-xs text-[#191c1e] dark:text-white"
                  />
                </div>
                <div>
                  <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Código Interno:</label>
                  <input
                    type="text"
                    value={newProduct.internalCode}
                    onChange={e => setNewProduct({ ...newProduct, internalCode: e.target.value })}
                    placeholder="PROD-101"
                    className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-mono text-xs text-[#191c1e] dark:text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Precio Venta ($ COP):</label>
                  <input
                    type="number"
                    value={newProduct.price}
                    onChange={e => setNewProduct({ ...newProduct, price: e.target.value })}
                    placeholder="12000"
                    className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-bold text-[#191c1e] dark:text-white"
                    required
                  />
                </div>
                <div>
                  <label className="font-bold text-gray-700 dark:text-gray-300 block mb-1">Stock Inicial:</label>
                  <input
                    type="number"
                    value={newProduct.stock}
                    onChange={e => setNewProduct({ ...newProduct, stock: e.target.value })}
                    placeholder="50"
                    className="w-full p-2.5 bg-[#f7f9fc] dark:bg-[#0b1411] border border-gray-300 dark:border-gray-700 rounded-2xl font-bold text-[#191c1e] dark:text-white"
                  />
                </div>
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="w-1/2 py-3 bg-gray-100 dark:bg-[#1e293b] hover:bg-gray-200 dark:hover:bg-[#334155] text-gray-700 dark:text-gray-300 rounded-2xl text-xs font-bold"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="w-1/2 py-3 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-2xl text-xs font-extrabold"
              >
                Guardar Producto
              </button>
            </div>
          </form>
        </div>
      )}

      <BarcodeModal product={barcodeProduct} onClose={() => setBarcodeProduct(null)} />
    </div>
  );
}
