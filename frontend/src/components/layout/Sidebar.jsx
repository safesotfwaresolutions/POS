import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { 
  LayoutDashboard, 
  ShoppingCart, 
  Package, 
  Tag, 
  Users, 
  FileText, 
  Settings, 
  ShieldCheck,
  Zap,
  LogOut,
  Store
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const navItems = [
    { path: '/', label: 'Dashboard Bento', icon: LayoutDashboard },
    { path: '/pos', label: 'Punto de Venta', icon: ShoppingCart, badge: 'Caja' },
    { path: '/inventory', label: 'Inventario', icon: Package },
    { path: '/products', label: 'Catálogo Productos', icon: Tag },
    { path: '/customers', label: 'Clientes', icon: Users },
    { path: '/invoicing', label: 'Facturación DIAN', icon: FileText, badge: 'Factus' },
    { path: '/settings', label: 'Configuración', icon: Settings },
  ];

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const getInitials = (name) => {
    if (!name) return 'US';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <aside className="fixed top-0 left-0 h-screen w-[248px] bg-[#101e19] text-white z-40 flex flex-col justify-between shadow-2xl select-none border-r border-[#1a2c25]">
      {/* Brand Header */}
      <div>
        <div className="px-5 py-5 border-b border-[#1a2c25] flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-[#006d3c] to-[#12b76a] flex items-center justify-center shadow-lg shadow-emerald-900/40 text-white font-black text-xl">
            <Zap className="w-6 h-6 fill-white text-white" />
          </div>
          <div>
            <h1 className="font-extrabold text-lg leading-tight tracking-tight text-white flex items-center gap-1.5">
              ProPOS <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-[#12b76a]/20 text-[#12b76a] border border-[#12b76a]/30">Bento</span>
            </h1>
            <p className="text-[11px] text-emerald-400/80 font-medium">Caja Principal • SUC-01</p>
          </div>
        </div>

        {/* Navigation Menu */}
        <nav className="p-3 space-y-1.5 mt-2">
          <p className="px-3 pb-2 text-[10px] uppercase font-bold tracking-wider text-emerald-500/70">
            Módulos del Sistema
          </p>

          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  `flex items-center justify-between px-3.5 py-2.5 rounded-2xl text-xs font-semibold transition-all duration-200 ${
                    isActive
                      ? 'bg-[#006d3c] text-white shadow-md shadow-[#006d3c]/40 font-bold'
                      : 'text-gray-300 hover:bg-[#1a2c25] hover:text-white'
                  }`
                }
              >
                <div className="flex items-center gap-3">
                  <Icon className="w-4.5 h-4.5" />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className="text-[10px] px-2 py-0.5 rounded-full font-bold bg-[#12b76a]/20 text-[#12b76a] border border-[#12b76a]/30">
                    {item.badge}
                  </span>
                )}
              </NavLink>
            );
          })}
        </nav>
      </div>

      {/* User Profile Footer */}
      <div className="p-3 border-t border-[#1a2c25] bg-[#0b1411] space-y-2">
        <div className="flex items-center justify-between p-2.5 rounded-2xl bg-[#14231e] border border-[#1d332c]">
          <div className="flex items-center gap-2.5 min-w-0">
            <div className="relative shrink-0">
              <div className="w-8 h-8 rounded-full bg-[#006d3c] flex items-center justify-center font-bold text-xs text-white shadow-sm">
                {getInitials(user?.fullName || user?.username)}
              </div>
              <span className="absolute bottom-0 right-0 w-2.5 h-2.5 rounded-full bg-[#12b76a] border-2 border-[#14231e]"></span>
            </div>
            <div className="truncate">
              <p className="text-xs font-bold text-white leading-tight truncate">
                {user?.fullName || user?.username || 'Usuario POS'}
              </p>
              <p className="text-[10px] text-emerald-400 font-semibold flex items-center gap-1">
                <ShieldCheck className="w-3 h-3 text-[#12b76a]" /> {user?.role || 'ADMINISTRATOR'}
              </p>
            </div>
          </div>
        </div>

        {/* Logout Button */}
        <button
          onClick={handleLogout}
          className="w-full py-2.5 bg-red-500/10 hover:bg-red-500/20 text-red-300 border border-red-500/30 rounded-2xl text-xs font-extrabold flex items-center justify-center gap-2 transition-colors cursor-pointer"
        >
          <LogOut className="w-3.5 h-3.5" />
          <span>Cerrar Sesión</span>
        </button>
      </div>
    </aside>
  );
}
