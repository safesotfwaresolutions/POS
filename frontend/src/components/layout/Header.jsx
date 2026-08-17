import React from 'react';
import { 
  Plus, 
  Search, 
  Bell, 
  Menu,
  Sun,
  Moon
} from 'lucide-react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';

export default function Header({ onToggleMobileMenu }) {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { darkMode, toggleTheme } = useTheme();

  const getInitials = (name) => {
    if (!name) return 'US';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <header className="bg-transparent flex justify-between items-center px-4 sm:px-8 py-4 h-20 w-full z-40 select-none">
      
      {/* Left Navigation Tabs */}
      <div className="flex items-center gap-6">
        <button
          onClick={onToggleMobileMenu}
          className="lg:hidden p-2 text-gray-700 dark:text-gray-200 hover:bg-gray-200 dark:hover:bg-[#334155] rounded-xl transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5 text-[#006d3c] dark:text-[#12b76a]" />
        </button>

        <div className="hidden md:flex gap-6 font-semibold text-sm text-[#191c1e] dark:text-gray-200">
          <NavLink 
            to="/" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] dark:text-white font-extrabold border-b-2 border-[#006d3c] dark:border-[#12b76a] pb-1" : "text-gray-500 dark:text-gray-400 hover:text-[#191c1e] dark:hover:text-white transition-colors"
            }
          >
            Dashboard
          </NavLink>
          <NavLink 
            to="/pos" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] dark:text-white font-extrabold border-b-2 border-[#006d3c] dark:border-[#12b76a] pb-1" : "text-gray-500 dark:text-gray-400 hover:text-[#191c1e] dark:hover:text-white transition-colors"
            }
          >
            Punto de Venta
          </NavLink>
          <NavLink 
            to="/invoicing" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] dark:text-white font-extrabold border-b-2 border-[#006d3c] dark:border-[#12b76a] pb-1" : "text-gray-500 dark:text-gray-400 hover:text-[#191c1e] dark:hover:text-white transition-colors"
            }
          >
            Facturas DIAN
          </NavLink>
          <NavLink 
            to="/inventory" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] dark:text-white font-extrabold border-b-2 border-[#006d3c] dark:border-[#12b76a] pb-1" : "text-gray-500 dark:text-gray-400 hover:text-[#191c1e] dark:hover:text-white transition-colors"
            }
          >
            Inventario
          </NavLink>
          <NavLink 
            to="/customers" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] dark:text-white font-extrabold border-b-2 border-[#006d3c] dark:border-[#12b76a] pb-1" : "text-gray-500 dark:text-gray-400 hover:text-[#191c1e] dark:hover:text-white transition-colors"
            }
          >
            Clientes
          </NavLink>
        </div>
      </div>

      {/* Right Action Tools: Dark/Light Mode, Search, Bell, Profile, + Nueva Venta */}
      <div className="flex items-center gap-3 sm:gap-4">
        {/* Dark / Light Mode Toggle Button */}
        <button 
          onClick={toggleTheme}
          className="p-2.5 text-gray-600 dark:text-amber-400 bg-white dark:bg-[#1e293b] border border-[#e0e3e6] dark:border-[#334155] hover:bg-[#e0e3e6] dark:hover:bg-[#334155] rounded-full transition-all flex items-center justify-center cursor-pointer shadow-xs"
          title={darkMode ? 'Cambiar a Modo Claro' : 'Cambiar a Modo Oscuro'}
        >
          {darkMode ? (
            <Sun className="w-4.5 h-4.5 text-amber-400 fill-amber-400/20" />
          ) : (
            <Moon className="w-4.5 h-4.5 text-slate-700" />
          )}
        </button>

        <button className="p-2.5 text-gray-600 dark:text-gray-300 hover:bg-[#e0e3e6] dark:hover:bg-[#334155] rounded-full transition-all flex items-center justify-center cursor-pointer">
          <Search className="w-4.5 h-4.5" />
        </button>

        <button className="p-2.5 text-gray-600 dark:text-gray-300 hover:bg-[#e0e3e6] dark:hover:bg-[#334155] rounded-full transition-all flex items-center justify-center relative cursor-pointer">
          <Bell className="w-4.5 h-4.5" />
          <span className="absolute top-2 right-2 w-2 h-2 bg-red-600 rounded-full border border-white"></span>
        </button>

        {/* User Profile Avatar */}
        <div className="w-10 h-10 rounded-full bg-[#006d3c] text-white flex items-center justify-center font-bold text-xs shadow-sm border-2 border-white dark:border-[#334155] cursor-pointer" title={user?.fullName || user?.username}>
          {getInitials(user?.fullName || user?.username)}
        </div>

        {/* Action Button "+ Nueva Venta" */}
        <button
          onClick={() => navigate('/pos')}
          className="flex items-center gap-1.5 bg-white dark:bg-[#1e293b] border border-[#bccabc] dark:border-[#334155] text-[#191c1e] dark:text-white font-bold text-xs rounded-full px-4 py-2 hover:bg-[#e0e3e6] dark:hover:bg-[#334155] transition-colors shadow-xs cursor-pointer"
        >
          <Plus className="w-4 h-4 text-[#006d3c] dark:text-[#12b76a]" />
          <span className="hidden sm:inline">Nueva Venta</span>
        </button>
      </div>

    </header>
  );
}
