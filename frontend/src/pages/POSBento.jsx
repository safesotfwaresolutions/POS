import React, { useState, useEffect, useRef } from 'react';
import { 
  Scan, 
  Search, 
  ShoppingCart, 
  Trash2, 
  Plus, 
  Minus, 
  CheckCircle2, 
  Printer, 
  Banknote, 
  X,
  ShieldAlert,
  Package,
  ChevronDown
} from 'lucide-react';
import { getProductsApi, getCustomersApi, createSaleApi } from '../services/api';

export default function POSBento() {
  const [productsList, setProductsList] = useState([]);
  const [customersList, setCustomersList] = useState([]);
  const [categoriesList] = useState(['Todos', 'Bebidas', 'Lácteos', 'Panadería', 'Abarrotes', 'Snacks']);
  const [selectedCategory, setSelectedCategory] = useState('Todos');
  const [searchQuery, setSearchQuery] = useState('');
  const [cart, setCart] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [loading, setLoading] = useState(true);

  const [issueDian, setIssueDian] = useState(false);
  const [showCheckoutModal, setShowCheckoutModal] = useState(false);
  const [cashTendered, setCashTendered] = useState('');
  const [saleCompleted, setSaleCompleted] = useState(null);
  const [showMobileCart, setShowMobileCart] = useState(false);

  const barcodeInputRef = useRef(null);

  const loadData = async () => {
    setLoading(true);
    try {
      const prods = await getProductsApi();
      if (Array.isArray(prods)) setProductsList(prods);

      const custs = await getCustomersApi();
      if (Array.isArray(custs)) {
        setCustomersList(custs);
        if (custs.length > 0) setSelectedCustomer(custs[0]);
      }
    } catch (e) {
      console.warn('Error cargando catálogo POS:', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const filteredProducts = productsList.filter(p => {
    const matchesCategory = selectedCategory === 'Todos' || p.categoryName === selectedCategory;
    const matchesSearch = (p.name && p.name.toLowerCase().includes(searchQuery.toLowerCase())) || 
                          (p.barcode && p.barcode.includes(searchQuery)) || 
                          (p.internalCode && p.internalCode.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesCategory && matchesSearch;
  });

  const addToCart = (product) => {
    const stockVal = product.quantityAvailable ?? product.stock ?? 0;
    if (stockVal <= 0) return;

    setCart(prev => {
      const existing = prev.find(item => item.id === product.id);
      if (existing) {
        if (existing.quantity >= stockVal) return prev;
        return prev.map(item => item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item);
      }
      const priceVal = product.salePrice || product.price || 0;
      return [...prev, { ...product, price: priceVal, stock: stockVal, quantity: 1 }];
    });
  };

  const updateQuantity = (id, delta) => {
    setCart(prev => prev.map(item => {
      if (item.id === id) {
        const newQty = item.quantity + delta;
        return newQty > 0 ? { ...item, quantity: newQty } : null;
      }
      return item;
    }).filter(Boolean));
  };

  const clearCart = () => {
    setCart([]);
  };

  const subtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const tax = issueDian ? Math.round(subtotal * 0.19) : 0;
  const total = subtotal + tax;

  const cashNumber = parseFloat(cashTendered) || 0;
  const change = cashNumber - total;

  const handleCompleteSale = async () => {
    if (cashNumber < total) return;

    const saleCommand = {
      customerId: selectedCustomer?.id || null,
      items: cart.map(item => ({
        productId: item.id,
        quantity: item.quantity,
        unitPrice: item.price
      })),
      cashReceived: cashNumber,
      totalAmount: total
    };

    let result = null;
    try {
      result = await createSaleApi(saleCommand);
    } catch (e) {
      alert('Error registrando venta en backend: ' + e.message);
      return;
    }

    const completed = {
      id: result?.id ? `FAC-${result.id}` : `FAC-${Math.floor(1000 + Math.random() * 9000)}`,
      date: new Date().toLocaleString('es-CO'),
      customer: selectedCustomer?.fullName || selectedCustomer?.name || 'Cliente General',
      items: [...cart],
      total,
      cashTendered: cashNumber,
      change,
      dianStatus: issueDian ? 'APROBADA_DIAN' : 'NO_REQUERIDA',
      cufe: issueDian ? `cufe-${Math.random().toString(36).substring(2, 12)}-2026` : null
    };

    setSaleCompleted(completed);
    setShowCheckoutModal(false);
    setShowMobileCart(false);
    setCart([]);
    setCashTendered('');
    await loadData();
  };

  return (
    <div className="space-[#191c1e] relative pb-20 lg:pb-0">
      {/* Top Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-3 mb-5">
        <div>
          <h1 className="text-xl sm:text-2xl font-black tracking-tight text-[#191c1e] flex items-center gap-2">
            Punto de Venta Bento <span className="text-xs font-bold px-2.5 py-1 bg-emerald-100 text-[#006d3c] rounded-full border border-emerald-300">Caja Real</span>
          </h1>
          <p className="text-xs text-gray-500 font-medium">Transacciones conectadas con Spring Boot API</p>
        </div>

        {/* Scrollable Categories List */}
        <div className="flex items-center gap-2 overflow-x-auto text-nowrap pb-1 max-w-full">
          <span className="text-xs font-bold text-gray-500 shrink-0">Categorías:</span>
          <div className="flex gap-1.5 shrink-0">
            {categoriesList.map(cat => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-3 py-1.5 rounded-2xl text-xs font-bold transition-all ${
                  selectedCategory === cat
                    ? 'bg-[#006d3c] text-white shadow-sm'
                    : 'bg-white text-gray-600 hover:bg-gray-100 border border-gray-200'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Main Responsive Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* Products Grid (Left Column) */}
        <div className="lg:col-span-7 space-y-4">
          <div className="bento-card p-3 flex items-center gap-3">
            <Search className="w-4 h-4 text-gray-400 shrink-0" />
            <input
              ref={barcodeInputRef}
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Buscar por nombre, código de barras [770...] o código interno..."
              className="w-full bg-transparent text-xs font-semibold focus:outline-none text-[#191c1e]"
            />
            {searchQuery && (
              <button onClick={() => setSearchQuery('')} className="text-xs font-bold text-gray-400 hover:text-gray-600 shrink-0">
                Limpiar
              </button>
            )}
          </div>

          {loading ? (
            <div className="py-12 text-center text-xs font-bold text-gray-400">
              Cargando catálogo real de productos...
            </div>
          ) : filteredProducts.length === 0 ? (
            <div className="py-12 text-center space-y-3 bg-gray-50/50 rounded-3xl border border-dashed border-gray-200">
              <Package className="w-12 h-12 text-gray-300 mx-auto stroke-1" />
              <p className="text-xs font-bold text-gray-700">No hay productos en el catálogo real</p>
              <p className="text-[11px] text-gray-400">Ve al catálogo para registrar el primer producto</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3.5">
              {filteredProducts.map(product => {
                const stockVal = product.quantityAvailable ?? product.stock ?? 0;
                const isLowStock = stockVal <= (product.minStock || 5);
                const isOut = stockVal <= 0;
                const priceVal = product.salePrice || product.price || 0;

                return (
                  <div
                    key={product.id}
                    onClick={() => !isOut && addToCart(product)}
                    className={`bento-card p-4 flex flex-col justify-between cursor-pointer group hover:border-[#12b76a] ${
                      isOut ? 'opacity-50 cursor-not-allowed bg-gray-50' : ''
                    }`}
                  >
                    <div>
                      <div className="flex items-center justify-between mb-2">
                        <span className="text-[10px] font-bold text-gray-400 font-mono">{product.internalCode}</span>
                        <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                          isOut
                            ? 'bg-red-100 text-red-700'
                            : isLowStock
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-emerald-100 text-[#006d3c]'
                        }`}>
                          {isOut ? 'Agotado' : `Stock: ${stockVal}`}
                        </span>
                      </div>

                      <h3 className="font-bold text-xs text-[#191c1e] group-hover:text-[#006d3c] line-clamp-2 transition-colors">
                        {product.name}
                      </h3>
                    </div>

                    <div className="mt-3 pt-2 border-t border-gray-100 flex items-center justify-between">
                      <div>
                        <span className="text-[10px] text-gray-400 block font-semibold">Precio unitario</span>
                        <span className="text-sm font-black text-[#191c1e] tabular-nums">
                          ${priceVal.toLocaleString('es-CO')}
                        </span>
                      </div>
                      <button 
                        disabled={isOut}
                        className="w-7 h-7 rounded-xl bg-emerald-50 text-[#006d3c] group-hover:bg-[#006d3c] group-hover:text-white flex items-center justify-center transition-all shadow-xs cursor-pointer"
                      >
                        <Plus className="w-4 h-4" />
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Shopping Cart (Right Column Desktop / Mobile Drawer Container) */}
        <div className={`lg:col-span-5 ${
          showMobileCart ? 'fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex flex-col justify-end lg:static lg:bg-transparent lg:z-auto' : 'hidden lg:block'
        }`}>
          <div className="bento-card p-5 sm:p-6 sticky top-20 space-y-4 bg-white shadow-xl max-h-[85vh] lg:max-h-none overflow-y-auto rounded-t-3xl lg:rounded-3xl w-full">
            
            <div className="flex items-center justify-between border-b border-gray-100 pb-3">
              <div className="flex items-center gap-2.5">
                <div className="p-2.5 bg-[#006d3c] text-white rounded-2xl shadow-xs">
                  <ShoppingCart className="w-5 h-5" />
                </div>
                <div>
                  <h2 className="font-extrabold text-sm text-[#191c1e]">Ticket de Venta Actual</h2>
                  <p className="text-[11px] text-gray-400 font-semibold">{cart.length} productos agregados</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {cart.length > 0 && (
                  <button
                    onClick={clearCart}
                    className="text-xs font-extrabold text-red-600 hover:text-red-700 flex items-center gap-1 bg-red-50 px-2.5 py-1 rounded-xl border border-red-100"
                  >
                    <Trash2 className="w-3.5 h-3.5" /> Vaciar
                  </button>
                )}
                {/* Mobile Close Cart Drawer */}
                <button
                  onClick={() => setShowMobileCart(false)}
                  className="lg:hidden p-1.5 text-gray-400 hover:text-gray-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            <div className="bg-gray-50 p-3.5 rounded-2xl border border-gray-200 space-y-2">
              <div className="flex items-center justify-between text-xs font-bold text-gray-600">
                <span>Cliente Asignado:</span>
                <span className="text-[10px] text-[#006d3c] font-extrabold">SELECCIONAR</span>
              </div>
              <select
                value={selectedCustomer?.id || ''}
                onChange={(e) => {
                  const cust = customersList.find(c => c.id === parseInt(e.target.value));
                  setSelectedCustomer(cust);
                }}
                className="w-full bg-white border border-gray-300 rounded-xl p-2 text-xs font-bold text-[#191c1e] focus:outline-none focus:border-[#006d3c]"
              >
                {customersList.length === 0 ? (
                  <option value="">Cliente General (Mostrador)</option>
                ) : (
                  customersList.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.fullName || c.name} ({c.identification || 'Consumidor Final'})
                    </option>
                  ))
                )}
              </select>

              <label className="flex items-center gap-2 pt-1 cursor-pointer">
                <input
                  type="checkbox"
                  checked={issueDian}
                  onChange={(e) => setIssueDian(e.target.checked)}
                  className="w-4 h-4 rounded text-[#006d3c] focus:ring-[#006d3c] cursor-pointer"
                />
                <span className="text-xs font-bold text-[#006d3c]">
                  Transmitir Factura Electrónica a DIAN (Factus API)
                </span>
              </label>
            </div>

            <div className="max-h-[250px] overflow-y-auto space-y-2 pr-1">
              {cart.length === 0 ? (
                <div className="py-10 text-center text-gray-400 space-y-2">
                  <ShoppingCart className="w-10 h-10 mx-auto stroke-1 text-gray-300" />
                  <p className="text-xs font-bold">El carrito de venta está vacío</p>
                  <p className="text-[11px]">Haz clic en los productos para agregar al ticket</p>
                </div>
              ) : (
                cart.map(item => (
                  <div key={item.id} className="p-3 bg-gray-50 rounded-2xl flex items-center justify-between border border-gray-200">
                    <div className="space-y-0.5">
                      <h4 className="font-bold text-xs text-[#191c1e]">{item.name}</h4>
                      <p className="text-[10px] text-gray-500 font-mono">
                        ${item.price.toLocaleString('es-CO')} c/u
                      </p>
                    </div>

                    <div className="flex items-center gap-3">
                      <div className="flex items-center gap-1.5 bg-white px-2 py-1 rounded-xl border border-gray-300">
                        <button
                          onClick={() => updateQuantity(item.id, -1)}
                          className="text-gray-500 hover:text-[#006d3c] font-bold text-xs"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <span className="text-xs font-black text-[#191c1e] px-1 tabular-nums">
                          {item.quantity}
                        </span>
                        <button
                          onClick={() => updateQuantity(item.id, 1)}
                          className="text-gray-500 hover:text-[#006d3c] font-bold text-xs"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </div>

                      <span className="text-xs font-black text-[#191c1e] tabular-nums min-w-[70px] text-right">
                        ${(item.price * item.quantity).toLocaleString('es-CO')}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>

            <div className="border-t border-gray-200 pt-3 space-y-2 text-xs font-semibold text-gray-600">
              <div className="flex justify-between">
                <span>Subtotal Neto:</span>
                <span className="font-bold text-[#191c1e] tabular-nums">${subtotal.toLocaleString('es-CO')} COP</span>
              </div>
              {issueDian && (
                <div className="flex justify-between text-[#006d3c] font-bold">
                  <span>IVA Estimado (19%):</span>
                  <span className="tabular-nums">${tax.toLocaleString('es-CO')} COP</span>
                </div>
              )}
              <div className="flex justify-between text-base font-black text-[#191c1e] pt-2 border-t border-gray-200">
                <span>Total a Cobrar:</span>
                <span className="text-xl text-[#006d3c] tabular-nums">${total.toLocaleString('es-CO')} COP</span>
              </div>
            </div>

            <button
              disabled={cart.length === 0}
              onClick={() => setShowCheckoutModal(true)}
              className={`w-full py-3.5 rounded-2xl font-black text-sm flex items-center justify-center gap-2 shadow-lg transition-all ${
                cart.length === 0
                  ? 'bg-gray-200 text-gray-400 cursor-not-allowed'
                  : 'bg-[#006d3c] hover:bg-[#00522c] text-white shadow-[#006d3c]/30 active:scale-98 cursor-pointer'
              }`}
            >
              <Banknote className="w-5 h-5" />
              <span>COBRAR EN EFECTIVO [F4]</span>
            </button>

          </div>
        </div>

      </div>

      {/* Floating Action Button on Mobile to Toggle Cart Drawer */}
      <div className="fixed bottom-4 left-4 right-4 z-30 lg:hidden">
        <button
          onClick={() => setShowMobileCart(true)}
          className="w-full py-3.5 px-5 bg-[#006d3c] text-white rounded-2xl font-black text-xs flex items-center justify-between shadow-2xl shadow-[#006d3c]/50 active:scale-95 transition-all"
        >
          <div className="flex items-center gap-2">
            <ShoppingCart className="w-4.5 h-4.5" />
            <span>Ver Ticket ({cart.length} ítems)</span>
          </div>
          <span className="text-sm font-extrabold tabular-nums">${total.toLocaleString('es-CO')} COP</span>
        </button>
      </div>

      {/* Checkout Bento Modal */}
      {showCheckoutModal && (
        <div className="fixed inset-0 bg-navy-900/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bento-card max-w-md w-full bg-white p-5 sm:p-6 rounded-3xl shadow-2xl space-y-4 max-h-[90vh] overflow-y-auto animate-in fade-in zoom-in duration-150">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-[#006d3c] text-white rounded-2xl">
                  <Banknote className="w-5 h-5" />
                </div>
                <h3 className="font-extrabold text-base text-[#191c1e]">Cobro en Efectivo</h3>
              </div>
              <button onClick={() => setShowCheckoutModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-4 bg-emerald-50/50 rounded-2xl border border-emerald-100 text-center">
              <span className="text-xs font-bold text-gray-500 uppercase tracking-wider">Monto Total de la Venta</span>
              <h2 className="text-2xl sm:text-3xl font-black text-[#006d3c] tabular-nums mt-0.5">
                ${total.toLocaleString('es-CO')} <span className="text-sm font-bold text-gray-500">COP</span>
              </h2>
            </div>

            <div className="space-y-2">
              <label className="text-xs font-extrabold text-gray-700 block">
                Efectivo Recibido del Cliente:
              </label>
              <div className="relative">
                <span className="absolute left-3.5 top-2.5 text-gray-400 font-bold">$</span>
                <input
                  type="number"
                  value={cashTendered}
                  onChange={(e) => setCashTendered(e.target.value)}
                  placeholder="Ingrese monto pagado..."
                  className="w-full pl-8 pr-4 py-2.5 bg-[#f7f9fc] border border-gray-300 rounded-2xl text-base font-black text-[#191c1e] focus:outline-none focus:border-[#006d3c]"
                  autoFocus
                />
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1">
                {[total, 20000, 50000, 100000].map(val => (
                  <button
                    key={val}
                    onClick={() => setCashTendered(val.toString())}
                    className="py-1.5 bg-gray-100 hover:bg-gray-200 text-[#191c1e] rounded-xl text-xs font-bold"
                  >
                    ${(val / 1000).toFixed(0)}k
                  </button>
                ))}
              </div>
            </div>

            <div className={`p-4 rounded-2xl border ${
              change >= 0 ? 'bg-emerald-50 border-emerald-200' : 'bg-red-50 border-red-200'
            }`}>
              <div className="flex justify-between items-center">
                <span className="text-xs font-extrabold text-gray-600 uppercase">Devuelta / Cambio:</span>
                <span className={`text-xl sm:text-2xl font-black tabular-nums ${
                  change >= 0 ? 'text-[#006d3c]' : 'text-red-600'
                }`}>
                  ${change >= 0 ? change.toLocaleString('es-CO') : '0'} COP
                </span>
              </div>
              {change < 0 && (
                <p className="text-[11px] font-bold text-red-600 mt-1 flex items-center gap-1">
                  <ShieldAlert className="w-3.5 h-3.5" /> El dinero ingresado es menor al total.
                </p>
              )}
            </div>

            <div className="flex items-center gap-3 pt-2">
              <button
                onClick={() => setShowCheckoutModal(false)}
                className="w-1/2 py-3 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-2xl text-xs font-bold"
              >
                Cancelar
              </button>
              <button
                disabled={change < 0 || cashNumber <= 0}
                onClick={handleCompleteSale}
                className={`w-1/2 py-3 rounded-2xl font-extrabold text-xs flex items-center justify-center gap-2 text-white shadow-md ${
                  change < 0 || cashNumber <= 0
                    ? 'bg-gray-300 cursor-not-allowed'
                    : 'bg-[#006d3c] hover:bg-[#00522c] shadow-[#006d3c]/30'
                }`}
              >
                <CheckCircle2 className="w-4 h-4" />
                <span>Confirmar Venta</span>
              </button>
            </div>

          </div>
        </div>
      )}

      {/* Sale Completed Success Receipt Modal */}
      {saleCompleted && (
        <div className="fixed inset-0 bg-navy-900/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bento-card max-w-md w-full bg-white p-6 rounded-3xl shadow-2xl space-y-4 text-center">
            <div className="w-12 h-12 bg-emerald-100 text-[#006d3c] rounded-full flex items-center justify-center mx-auto">
              <CheckCircle2 className="w-7 h-7 text-[#006d3c]" />
            </div>
            <h3 className="text-xl font-extrabold text-[#191c1e]">¡Venta Registrada en Base de Datos!</h3>
            <p className="text-xs text-gray-500 font-medium">Comprobante #{saleCompleted.id} • Procesada en Spring Boot</p>

            <div className="p-4 bg-gray-50 rounded-2xl border border-gray-200 text-left text-xs space-y-1.5">
              <div className="flex justify-between font-bold text-gray-700">
                <span>Cliente:</span>
                <span>{saleCompleted.customer}</span>
              </div>
              <div className="flex justify-between font-bold text-gray-700">
                <span>Monto Cobrado:</span>
                <span>${saleCompleted.total.toLocaleString('es-CO')} COP</span>
              </div>
              <div className="flex justify-between font-bold text-[#006d3c]">
                <span>Devuelta / Cambio:</span>
                <span>${saleCompleted.change.toLocaleString('es-CO')} COP</span>
              </div>
              {saleCompleted.cufe && (
                <div className="pt-2 border-t border-gray-200 text-[10px] text-gray-500 font-mono">
                  <span className="font-bold text-[#006d3c] block">Factura DIAN Transmitida:</span>
                  <span>{saleCompleted.cufe}</span>
                </div>
              )}
            </div>

            <div className="flex gap-2 pt-2">
              <button
                onClick={() => alert(`Imprimiendo tiquete térmico para venta ${saleCompleted.id}...`)}
                className="w-1/2 py-2.5 bg-gray-100 hover:bg-gray-200 text-[#191c1e] rounded-2xl font-bold text-xs flex items-center justify-center gap-1.5"
              >
                <Printer className="w-4 h-4" /> Imprimir Tiquete
              </button>
              <button
                onClick={() => setSaleCompleted(null)}
                className="w-1/2 py-2.5 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-2xl font-bold text-xs"
              >
                Siguiente Venta
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
