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
  X,
  History
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

export default function Sidebar({ isOpen, onClose }) {
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

      {/* Sidebar - Compact Icon-only Sidebar matching Stitch MCP Screenshot */}
      <aside 
        className={`fixed top-0 left-0 h-screen w-20 lg:w-20 bg-white z-50 flex flex-col justify-between items-center py-6 border-r border-[#e0e3e6] shadow-xs select-none transition-transform duration-300 ${
          isOpen ? 'translate-x-0 w-64' : '-translate-x-full lg:translate-x-0'
        }`}
      >
        <div className="flex flex-col items-center gap-6 w-full px-3">
          {/* Top Brand Q Circle */}
          <div className="w-10 h-10 rounded-full bg-[#12b76a] text-white flex items-center justify-center font-black text-xl shadow-sm cursor-pointer">
            Q
          </div>

          {/* Navigation Menu */}
          <nav className="flex flex-col gap-3 w-full items-center mt-2">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <NavLink
                  key={item.path}
                  to={item.path}
                  onClick={handleNavClick}
                  title={item.label}
                  className={({ isActive }) =>
                    `p-3 rounded-2xl flex items-center justify-center transition-all duration-200 cursor-pointer ${
                      isActive
                        ? 'bg-[#006d3c] text-white shadow-md shadow-[#006d3c]/20 scale-105'
                        : 'text-gray-500 hover:bg-[#eceef1] hover:text-[#006d3c]'
                    } ${isOpen ? 'w-full justify-start gap-3 px-4' : 'w-11 h-11'}`
                  }
                >
                  <Icon className="w-5 h-5 shrink-0" />
                  {isOpen && <span className="text-xs font-bold text-[#191c1e]">{item.label}</span>}
                </NavLink>
              );
            })}
          </nav>
        </div>

        {/* Bottom Support & Logout */}
        <div className="flex flex-col gap-3 w-full items-center px-3">
          <button 
            title="Soporte"
            className="p-3 text-gray-500 hover:bg-[#eceef1] hover:text-[#006d3c] rounded-2xl transition-all cursor-pointer w-11 h-11 flex items-center justify-center"
          >
            <HelpCircle className="w-5 h-5" />
          </button>
          <button
            onClick={handleLogout}
            title="Cerrar Sesión"
            className="p-3 text-red-500 hover:bg-red-50 rounded-2xl transition-all cursor-pointer w-11 h-11 flex items-center justify-center"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </aside>
    </>
  );
}
