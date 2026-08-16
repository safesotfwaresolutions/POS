import React from 'react';
import { 
  Plus, 
  Search, 
  Bell, 
  Menu
} from 'lucide-react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Header({ onToggleMobileMenu }) {
  const navigate = useNavigate();
  const { user } = useAuth();

  const getInitials = (name) => {
    if (!name) return 'US';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  };

  return (
    <header className="bg-transparent flex justify-between items-center px-6 sm:px-8 py-4 h-20 w-full z-40 select-none">
      
      {/* Left Navigation Tabs from Stitch MCP Screenshot */}
      <div className="flex items-center gap-6">
        <button
          onClick={onToggleMobileMenu}
          className="lg:hidden p-2 text-gray-700 hover:bg-gray-200 rounded-xl transition-colors cursor-pointer"
        >
          <Menu className="w-5 h-5 text-[#006d3c]" />
        </button>

        <div className="hidden md:flex gap-6 font-semibold text-sm text-[#191c1e]">
          <NavLink 
            to="/" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] font-extrabold border-b-2 border-[#006d3c] pb-1" : "text-gray-500 hover:text-[#191c1e] transition-colors"
            }
          >
            Dashboard
          </NavLink>
          <NavLink 
            to="/pos" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] font-extrabold border-b-2 border-[#006d3c] pb-1" : "text-gray-500 hover:text-[#191c1e] transition-colors"
            }
          >
            Punto de Venta
          </NavLink>
          <NavLink 
            to="/invoicing" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] font-extrabold border-b-2 border-[#006d3c] pb-1" : "text-gray-500 hover:text-[#191c1e] transition-colors"
            }
          >
            Facturas DIAN
          </NavLink>
          <NavLink 
            to="/inventory" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] font-extrabold border-b-2 border-[#006d3c] pb-1" : "text-gray-500 hover:text-[#191c1e] transition-colors"
            }
          >
            Inventario
          </NavLink>
          <NavLink 
            to="/customers" 
            className={({ isActive }) => 
              isActive ? "text-[#191c1e] font-extrabold border-b-2 border-[#006d3c] pb-1" : "text-gray-500 hover:text-[#191c1e] transition-colors"
            }
          >
            Clientes
          </NavLink>
        </div>
      </div>

      {/* Right Action Tools from Stitch MCP Screenshot */}
      <div className="flex items-center gap-3.5">
        <button className="p-2 text-[#3d4a3f] hover:bg-[#e0e3e6] rounded-full transition-all flex items-center justify-center cursor-pointer">
          <Search className="w-5 h-5" />
        </button>

        <button className="p-2 text-[#3d4a3f] hover:bg-[#e0e3e6] rounded-full transition-all flex items-center justify-center relative cursor-pointer">
          <Bell className="w-5 h-5" />
          <span className="absolute top-2 right-2 w-2 h-2 bg-red-600 rounded-full border border-white"></span>
        </button>

        {/* User Profile Avatar */}
        <div className="w-10 h-10 rounded-full bg-[#006d3c] text-white flex items-center justify-center font-bold text-xs shadow-sm border-2 border-white cursor-pointer" title={user?.fullName || user?.username}>
          {getInitials(user?.fullName || user?.username)}
        </div>

        {/* Action Button "+ Nueva Venta" */}
        <button
          onClick={() => navigate('/pos')}
          className="flex items-center gap-1.5 bg-white border border-[#bccabc] text-[#191c1e] font-bold text-xs rounded-full px-4 py-2 hover:bg-[#e0e3e6] transition-colors shadow-xs cursor-pointer"
        >
          <Plus className="w-4 h-4 text-[#006d3c]" />
          <span>Nueva Venta</span>
        </button>
      </div>

    </header>
  );
}
