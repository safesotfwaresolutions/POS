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
  HelpCircle,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Zap,
  ShieldCheck
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Sidebar({ isOpen, onClose, isCollapsed, onToggleCollapse }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const navItems = [
    { path: '/', label: 'Dashboard', icon: LayoutDashboard },
    { path: '/pos', label: 'Punto de Venta', icon: ShoppingCart },
    { path: '/products', label: 'Productos', icon: Tag },
    { path: '/inventory', label: 'Inventario', icon: Package },
    { path: '/customers', label: 'Clientes', icon: Users },
    { path: '/invoicing', label: 'Facturación DIAN', icon: FileText },
    { path: '/settings', label: 'Configuración', icon: Settings },
    ...(user?.role === 'SUPER_ADMIN'
      ? [{ path: '/backoffice', label: 'Backoffice', icon: ShieldCheck }]
      : []),
  ];

  const handleLogout = async () => {
    await logout();
    if (onClose) onClose();
    navigate('/login');
  };

  const handleNavClick = () => {
    if (onClose) onClose();
  };

  return (
    <>
      {/* Mobile Backdrop Overlay */}
      {isOpen && (
        <div 
          onClick={onClose}
          className="fixed inset-0 bg-black/60 backdrop-blur-xs z-40 lg:hidden transition-opacity"
        />
      )}

      {/* Sidebar - Expandable / Collapsible Sidebar */}
      <aside 
        className={`fixed top-0 left-0 h-screen bg-white dark:bg-[#1e293b] text-[#191c1e] dark:text-white z-50 flex flex-col justify-between py-6 border-r border-[#e0e3e6] dark:border-[#334155] shadow-xs select-none transition-all duration-300 ${
          isCollapsed ? 'lg:w-20' : 'lg:w-64'
        } ${
          isOpen ? 'translate-x-0 w-64' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <div className="flex flex-col items-center w-full px-3">
          {/* Brand Header */}
          <div className={`flex items-center gap-3 w-full pb-4 border-b border-[#e0e3e6] dark:border-[#334155] ${
            isCollapsed ? 'justify-center' : 'px-2 justify-between'
          }`}>
            <div className="flex items-center gap-2.5 min-w-0">
              <div className="w-10 h-10 rounded-2xl bg-[#006d3c] text-white flex items-center justify-center font-black text-xl shadow-md shrink-0">
                <Zap className="w-5 h-5 fill-white" />
              </div>
              {(!isCollapsed || isOpen) && (
                <div className="truncate">
                  <h1 className="font-extrabold text-base leading-tight text-[#191c1e] dark:text-white truncate">
                    ProPOS <span className="text-xs font-semibold px-1.5 py-0.5 rounded-full bg-[#12b76a]/20 text-[#006d3c] dark:text-[#12b76a]">Bento</span>
                  </h1>
                  <p className="text-[10px] text-gray-400 font-medium">Caja Principal</p>
                </div>
              )}
            </div>
          </div>

          {/* Navigation Menu */}
          <nav className="flex flex-col gap-2 w-full mt-4">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  onClick={handleNavClick}
                  title={isCollapsed ? item.label : undefined}
                  className={({ isActive }) =>
                    `rounded-2xl flex items-center transition-all duration-200 cursor-pointer ${
                      isActive
                        ? 'bg-[#006d3c] text-white shadow-md shadow-[#006d3c]/20 font-bold'
                        : 'text-gray-600 dark:text-gray-300 hover:bg-[#f2f4f7] dark:hover:bg-[#334155] hover:text-[#006d3c] dark:hover:text-white'
                    } ${
                      isCollapsed && !isOpen
                        ? 'w-11 h-11 justify-center mx-auto'
                        : 'w-full px-3.5 py-3 gap-3 text-xs font-semibold'
                    }`
                  }
                >
                  <Icon className="w-5 h-5 shrink-0" />
                  {(!isCollapsed || isOpen) && <span className="truncate">{item.label}</span>}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Bottom Tools & Collapse Toggle */}
        <div className="flex flex-col gap-2 w-full px-3 pt-3 border-t border-[#e0e3e6] dark:border-[#334155]">
          {/* Collapse/Expand Desktop Button */}
          <button
            onClick={onToggleCollapse}
            className={`hidden lg:flex items-center gap-3 p-2.5 rounded-2xl text-xs font-bold text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-[#334155] transition-colors cursor-pointer ${
              isCollapsed ? 'justify-center' : 'px-3'
            }`}
            title={isCollapsed ? 'Expandir Menú' : 'Contraer Menú'}
          >
            {isCollapsed ? (
              <ChevronRight className="w-5 h-5 text-[#006d3c] dark:text-[#12b76a]" />
            ) : (
              <>
                <ChevronLeft className="w-5 h-5 text-[#006d3c] dark:text-[#12b76a]" />
                <span>Contraer Menú</span>
              </>
            )}
          </button>

          {/* Help Button */}
          <button 
            title="Soporte"
            className={`flex items-center gap-3 p-2.5 text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-[#334155] hover:text-[#006d3c] rounded-2xl transition-all cursor-pointer text-xs font-semibold ${
              isCollapsed && !isOpen ? 'justify-center' : 'px-3'
            }`}
          >
            <HelpCircle className="w-5 h-5 shrink-0" />
            {(!isCollapsed || isOpen) && <span>Soporte</span>}
          </button>

          {/* Logout Button */}
          <button
            onClick={handleLogout}
            title="Cerrar Sesión"
            className={`flex items-center gap-3 p-2.5 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-950/30 rounded-2xl transition-all cursor-pointer text-xs font-extrabold ${
              isCollapsed && !isOpen ? 'justify-center' : 'px-3'
            }`}
          >
            <LogOut className="w-5 h-5 shrink-0" />
            {(!isCollapsed || isOpen) && <span>Cerrar Sesión</span>}
          </button>
        </div>
      </aside>
    </>
  );
}
