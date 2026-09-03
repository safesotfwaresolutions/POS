import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  ShoppingCart,
  Package,
  Tag,
  Truck,
  Users,
  FileText,
  FileCheck2,
  BarChart3,
  RotateCcw,
  Settings,
  HelpCircle,
  LogOut,
  ChevronLeft,
  ChevronRight,
  Zap
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Sidebar({ isOpen, onClose, isCollapsed, onToggleCollapse }) {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  // Agrupado por área de trabajo para que el menú sea más fácil de escanear.
  // El backend no expone ningún reporte a SELLER (solo Admin y Supervisor).
  const navGroups = [
    {
      title: 'Principal',
      items: [
        { path: '/', label: 'Dashboard', icon: LayoutDashboard },
      ],
    },
    {
      title: 'Operación',
      items: [
        { path: '/pos', label: 'Punto de Venta', icon: ShoppingCart },
        { path: '/returns', label: 'Devoluciones', icon: RotateCcw },
        { path: '/products', label: 'Productos', icon: Tag },
        { path: '/inventory', label: 'Inventario', icon: Package },
        { path: '/suppliers', label: 'Proveedores', icon: Truck },
      ],
    },
    {
      title: 'Clientes y Facturación',
      items: [
        { path: '/customers', label: 'Clientes', icon: Users },
        { path: '/invoicing', label: 'Facturación DIAN', icon: FileText },
        { path: '/documents', label: 'Documentos', icon: FileCheck2 },
      ],
    },
    ...(user?.role !== 'SELLER' ? [{
      title: 'Análisis',
      items: [{ path: '/reports', label: 'Reportes', icon: BarChart3 }],
    }] : []),
    {
      title: 'Sistema',
      items: [
        { path: '/settings', label: 'Configuración', icon: Settings },
      ],
    },
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
        className={`fixed top-0 left-0 h-screen bg-white dark:bg-[#1e293b] text-[#191c1e] dark:text-white z-50 flex flex-col py-6 border-r border-[#e0e3e6] dark:border-[#334155] shadow-xs select-none transition-all duration-300 ${
          isCollapsed ? 'lg:w-20' : 'lg:w-64'
        } ${
          isOpen ? 'translate-x-0 w-64' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        {/* Brand Header */}
        <div className={`shrink-0 flex items-center gap-3 w-full pb-4 border-b border-[#e0e3e6] dark:border-[#334155] ${
          isCollapsed ? 'px-3 justify-center' : 'px-5 justify-between'
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

        {/* Navigation Menu, agrupado por área de trabajo. Scrollable: con muchos grupos no debe
            empujar "Soporte"/"Cerrar Sesión" fuera de la pantalla. */}
        <nav className="flex-1 min-h-0 overflow-y-auto flex flex-col gap-4 w-full px-3 mt-4">
            {navGroups.map((group, groupIdx) => (
              <div key={group.title} className="flex flex-col gap-2 w-full">
                {(!isCollapsed || isOpen) ? (
                  <p className="px-3.5 text-[10px] font-extrabold uppercase tracking-wider text-gray-400 dark:text-gray-500">
                    {group.title}
                  </p>
                ) : (
                  groupIdx > 0 && <div className="h-px w-8 bg-[#e0e3e6] dark:bg-[#334155] mx-auto" />
                )}
                {group.items.map((item) => {
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
              </div>
            ))}
        </nav>

        {/* Bottom Tools & Collapse Toggle */}
        <div className="shrink-0 flex flex-col gap-2 w-full px-3 pt-3 border-t border-[#e0e3e6] dark:border-[#334155]">
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
