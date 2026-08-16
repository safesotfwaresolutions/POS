import React from 'react';
import { NavLink } from 'react-router-dom';
import { 
  LayoutDashboard, 
  ShoppingCart, 
  Package, 
  Tag, 
  Users, 
  FileText, 
  Settings, 
  Store,
  ShieldCheck,
  Zap
} from 'lucide-react';

export default function Sidebar({ activeRole = 'Vendedor / Cajero' }) {
  const navItems = [
    { path: '/', label: 'Dashboard Bento', icon: LayoutDashboard },
    { path: '/pos', label: 'Punto de Venta', icon: ShoppingCart, badge: 'Caja' },
    { path: '/inventory', label: 'Inventario', icon: Package, badge: '5 alertas' },
    { path: '/products', label: 'Catálogo Productos', icon: Tag },
    { path: '/customers', label: 'Clientes', icon: Users },
    { path: '/invoicing', label: 'Facturación DIAN', icon: FileText, badge: 'Factus' },
    { path: '/settings', label: 'Configuración', icon: Settings },
  ];

  return (
    <aside className="fixed top-0 left-0 h-screen w-[248px] bg-[#101828] text-white z-40 flex flex-col justify-between shadow-xl select-none">
      {/* Brand Header */}
      <div>
        <div className="px-5 py-5 border-b border-[#1d2939] flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-700 to-blue-500 flex items-center justify-center shadow-lg shadow-blue-500/20 text-white font-black text-xl">
            <Zap className="w-6 h-6 fill-white text-white" />
          </div>
          <div>
            <h1 className="font-extrabold text-lg leading-tight tracking-tight text-white flex items-center gap-1.5">
              ProPOS <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-600/30 text-blue-300 border border-blue-500/40">Bento</span>
            </h1>
            <p className="text-[11px] text-gray-400 font-medium">Caja Principal • SUC-01</p>
          </div>
        </div>

        {/* Navigation Menu */}
        <nav className="p-3 space-y-1 mt-2">
          <p className="px-3 pb-2 text-[10px] uppercase font-bold tracking-wider text-gray-400">
            Módulos del Sistema
          </p>

          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all duration-150 ${
                    isActive
                      ? 'bg-blue-600 text-white shadow-md shadow-blue-600/30 font-bold'
                      : 'text-gray-300 hover:bg-[#1d2939] hover:text-white'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon className="w-4 h-4" />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-bold ${
                    item.badge.includes('alertas') 
                      ? 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
                      : 'bg-white/10 text-white'
                  }`}>
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* Cashier & System Status Footer */}
      <div className="p-3 border-t border-[#1d2939] bg-[#0c121e]">
        <div className="flex items-center justify-between p-2.5 rounded-xl bg-[#1a2333] border border-[#253247]">
          <div className="flex items-center gap-2.5">
            <div className="relative">
              <div className="w-8 h-8 rounded-full bg-blue-600 flex items-center justify-center font-bold text-xs text-white">
                JS
              </div>
              <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-emerald-500 border-2 border-[#1a2333]"></span>
            </div>
            <div>
              <p className="text-xs font-bold text-white leading-tight">Juan Sebastián</p>
              <p className="text-[10px] text-emerald-400 font-medium flex items-center gap-1">
                <ShieldCheck className="w-3 h-3" /> {activeRole}
              </p>
            </div>
          </div>
          <div className="text-right">
            <span className="text-[9px] uppercase font-bold text-gray-400 block">Caja #1</span>
            <span className="text-[10px] font-extrabold text-emerald-400">ABIERTA</span>
          </div>
        </div>
      </div>
    </aside>
  );
}
