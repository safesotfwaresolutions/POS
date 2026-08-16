import React, { useState } from 'react';
import { 
  Tag, 
  Plus, 
  Search, 
  Edit3, 
  Trash2, 
  Barcode, 
  Check, 
  X,
  Sparkles
} from 'lucide-react';
import { INITIAL_MOCK_DATA } from '../services/api';

export default function ProductsBento() {
  const [products, setProducts] = useState(INITIAL_MOCK_DATA.products);
  const [search, setSearch] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [newProduct, setNewProduct] = useState({
    name: '',
    barcode: '',
    internalCode: '',
    category: 'Abarrotes',
    price: '',
    stock: '',
    minStock: 5
  });

  const handleAddProduct = (e) => {
    e.preventDefault();
    if (!newProduct.name || !newProduct.price) return;

    const item = {
      id: Date.now(),
      name: newProduct.name,
      barcode: newProduct.barcode || `${Math.floor(1000000000000 + Math.random() * 9000000000000)}`,
      internalCode: newProduct.internalCode || `PROD-00${products.length + 1}`,
      category: newProduct.category,
      price: parseFloat(newProduct.price),
      stock: parseInt(newProduct.stock) || 0,
      minStock: parseInt(newProduct.minStock) || 5,
      unit: 'Unidad'
    };

    setProducts([item, ...products]);
    setShowAddModal(false);
    setNewProduct({ name: '', barcode: '', internalCode: '', category: 'Abarrotes', price: '', stock: '', minStock: 5 });
  };

  const filtered = products.filter(p => 
    p.name.toLowerCase().includes(search.toLowerCase()) || 
    p.barcode.includes(search)
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-[#111c2d]">Catálogo de Productos Bento</h1>
          <p className="text-xs text-gray-500 font-medium">Gestión de maestro de productos, precios y asignación de código de barras</p>
        </div>

        <button
          onClick={() => setShowAddModal(true)}
          className="px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold flex items-center gap-2 shadow-md shadow-blue-600/20"
        >
          <Plus className="w-4 h-4" /> Crear Nuevo Producto
        </button>
      </div>

      <div className="bento-card p-6 space-y-4">
        <div className="flex items-center justify-between">
          <div className="relative w-72">
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Filtrar por nombre o código de barras..."
              className="w-full pl-9 pr-3 py-1.5 bg-gray-50 border border-gray-200 rounded-xl text-xs font-semibold focus:outline-none focus:border-blue-600"
            />
          </div>
          <span className="text-xs font-bold text-gray-400">{filtered.length} productos listados</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map(p => (
            <div key={p.id} className="p-4 bg-gray-50/60 rounded-xl border border-gray-200 space-y-3 flex flex-col justify-between hover:bg-white hover:border-blue-300 transition-all shadow-xs">
              <div>
                <div className="flex items-center justify-between">
                  <span className="text-[10px] px-2 py-0.5 rounded-full bg-blue-100 text-blue-800 font-extrabold">{p.category}</span>
                  <span className="text-[10px] font-mono text-gray-400 font-bold">{p.internalCode}</span>
                </div>
                <h3 className="font-bold text-sm text-[#111c2d] mt-2">{p.name}</h3>
                <p className="text-[11px] text-gray-500 font-mono mt-0.5 flex items-center gap-1">
                  <Barcode className="w-3.5 h-3.5 text-gray-400" /> {p.barcode}
                </p>
              </div>

              <div className="pt-3 border-t border-gray-200 flex items-center justify-between">
                <div>
                  <span className="text-[10px] text-gray-400 block font-semibold">Precio de venta</span>
                  <span className="text-base font-black text-[#111c2d] tabular-nums">
                    ${p.price.toLocaleString('es-CO')} COP
                  </span>
                </div>

                <div className="flex items-center gap-1">
                  <button className="p-1.5 text-gray-400 hover:text-blue-600 rounded-lg hover:bg-gray-100">
                    <Edit3 className="w-4 h-4" />
                  </button>
                  <button onClick={() => setProducts(products.filter(item => item.id !== p.id))} className="p-1.5 text-gray-400 hover:text-red-600 rounded-lg hover:bg-gray-100">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Add Product Modal */}
      {showAddModal && (
        <div className="fixed inset-0 bg-navy-900/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <form onSubmit={handleAddProduct} className="bento-card max-w-md w-full bg-white p-6 rounded-2xl shadow-2xl space-y-4">
            <h3 className="font-extrabold text-base text-[#111c2d]">Crear Nuevo Producto</h3>

            <div className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-gray-700 block mb-1">Nombre Comercial del Producto:</label>
                <input
                  type="text"
                  value={newProduct.name}
                  onChange={e => setNewProduct({ ...newProduct, name: e.target.value })}
                  placeholder="Ej. Galletas Saltín Noel x3"
                  className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-semibold"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-gray-700 block mb-1">Código de Barras EAN:</label>
                  <input
                    type="text"
                    value={newProduct.barcode}
                    onChange={e => setNewProduct({ ...newProduct, barcode: e.target.value })}
                    placeholder="770..."
                    className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-mono"
                  />
                </div>
                <div>
                  <label className="font-bold text-gray-700 block mb-1">Categoría:</label>
                  <select
                    value={newProduct.category}
                    onChange={e => setNewProduct({ ...newProduct, category: e.target.value })}
                    className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-semibold"
                  >
                    {INITIAL_MOCK_DATA.categories.filter(c => c !== 'Todos').map(c => (
                      <option key={c} value={c}>{c}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-gray-700 block mb-1">Precio Venta ($ COP):</label>
                  <input
                    type="number"
                    value={newProduct.price}
                    onChange={e => setNewProduct({ ...newProduct, price: e.target.value })}
                    placeholder="15000"
                    className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-bold"
                    required
                  />
                </div>
                <div>
                  <label className="font-bold text-gray-700 block mb-1">Stock Inicial:</label>
                  <input
                    type="number"
                    value={newProduct.stock}
                    onChange={e => setNewProduct({ ...newProduct, stock: e.target.value })}
                    placeholder="20"
                    className="w-full p-2 bg-gray-50 border border-gray-300 rounded-xl font-bold"
                  />
                </div>
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <button
                type="button"
                onClick={() => setShowAddModal(false)}
                className="w-1/2 py-2.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs font-bold"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="w-1/2 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-extrabold"
              >
                Guardar Producto
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
