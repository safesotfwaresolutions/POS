import React, { useState, useEffect } from 'react';
import { 
  Scan, 
  PlusCircle, 
  Bell, 
  Search, 
  CheckCircle2, 
  Clock, 
  UserCheck,
  Zap
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function Header({ activeRole, setActiveRole, onBarcodeSearch }) {
  const navigate = useNavigate();
  const [currentTime, setCurrentTime] = useState('');
  const [searchValue, setSearchValue] = useState('');

  useEffect(() => {
    const updateClock = () => {
      const now = new Date();
      setCurrentTime(
        now.toLocaleDateString('es-CO', { weekday: 'short', day: 'numeric', month: 'short' }) +
        ' • ' +
        now.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' })
      );
    };
    updateClock();
    const interval = setInterval(updateClock, 1000);
    return () => clearInterval(interval);
  }, []);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    if (onBarcodeSearch) {
      onBarcodeSearch(searchValue);
    }
  };

  return (
    <header className="sticky top-0 z-30 bg-white/90 backdrop-blur-md border-b border-[#e4e7ec] px-6 py-3 flex items-center justify-between shadow-xs">
      {/* Search & Barcode Scanner Quick Input */}
      <form onSubmit={handleSearchSubmit} className="relative w-full max-w-md">
        <div className="relative flex items-center">
          <Search className="absolute left-3.5 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            placeholder="Buscar producto o escanear código de barras [F2]..."
            className="w-full pl-10 pr-24 py-2 bg-[#f8fafc] border border-[#e4e7ec] rounded-xl text-xs text-[#111c2d] placeholder-gray-400 focus:outline-none focus:border-blue-600 focus:bg-white transition-all shadow-inner"
          />
          <button
            type="submit"
            className="absolute right-2 px-2 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-[10px] font-bold flex items-center gap-1 transition-colors"
          >
            <Scan className="w-3 h-3" /> ESCANEAR
          </button>
        </div>
      </form>

      {/* Center/Right Status Indicators */}
      <div className="flex items-center gap-4">
        {/* Real-time Clock */}
        <div className="hidden lg:flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 rounded-lg text-xs font-semibold text-gray-600 border border-gray-200">
          <Clock className="w-3.5 h-3.5 text-blue-600" />
          <span>{currentTime}</span>
        </div>

        {/* DIAN Factus Status */}
        <div className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 text-emerald-700 rounded-lg text-xs font-bold border border-emerald-200">
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
          <span className="hidden sm:inline">Factus DIAN</span>
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
        </div>

        {/* Role Switcher Demo Control */}
        <div className="flex items-center gap-1 bg-gray-100 p-1 rounded-xl border border-gray-200">
          <UserCheck className="w-3.5 h-3.5 text-gray-500 ml-1.5" />
          <select 
            value={activeRole} 
            onChange={(e) => setActiveRole(e.target.value)}
            className="bg-transparent text-xs font-bold text-gray-700 focus:outline-none pr-1 cursor-pointer"
          >
            <option value="Vendedor">Rol: Vendedor</option>
            <option value="Supervisor">Rol: Supervisor</option>
            <option value="Administrador">Rol: Administrador</option>
          </select>
        </div>

        {/* Quick Sale Action Button */}
        <button
          onClick={() => navigate('/pos')}
          className="px-4 py-2 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white rounded-xl font-bold text-xs flex items-center gap-2 shadow-md shadow-blue-500/20 active:scale-95 transition-all"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Nueva Venta</span>
        </button>
      </div>
    </header>
  );
}
