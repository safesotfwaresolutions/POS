import React, { useState, useEffect } from 'react';
import { 
  Scan, 
  PlusCircle, 
  Search, 
  CheckCircle2, 
  Clock, 
  LogOut,
  ShieldCheck,
  User
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Header({ onBarcodeSearch }) {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
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

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-30 bg-white/90 backdrop-blur-md border-b border-[#e0e3e6] px-8 py-3.5 flex items-center justify-between shadow-xs">
      {/* Search & Barcode Scanner Quick Input */}
      <form onSubmit={handleSearchSubmit} className="relative w-full max-w-md">
        <div className="relative flex items-center">
          <Search className="absolute left-3.5 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            placeholder="Buscar producto o escanear código de barras [F2]..."
            className="w-full pl-10 pr-24 py-2 bg-[#f7f9fc] border border-[#e0e3e6] rounded-2xl text-xs text-[#191c1e] placeholder-gray-400 focus:outline-none focus:border-[#006d3c] focus:bg-white transition-all shadow-inner"
          />
          <button
            type="submit"
            className="absolute right-2 px-2.5 py-1 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-xl text-[10px] font-bold flex items-center gap-1 transition-colors shadow-xs"
          >
            <Scan className="w-3 h-3" /> ESCANEAR
          </button>
        </div>
      </form>

      {/* Center/Right Status Indicators */}
      <div className="flex items-center gap-4">
        {/* Real-time Clock */}
        <div className="hidden lg:flex items-center gap-1.5 px-3 py-1.5 bg-gray-100 rounded-xl text-xs font-semibold text-gray-600 border border-gray-200">
          <Clock className="w-3.5 h-3.5 text-[#006d3c]" />
          <span>{currentTime}</span>
        </div>

        {/* DIAN Factus Status */}
        <div className="flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 text-[#006d3c] rounded-xl text-xs font-extrabold border border-emerald-200">
          <CheckCircle2 className="w-3.5 h-3.5 text-[#12b76a]" />
          <span className="hidden sm:inline">Factus API DIAN</span>
          <span className="w-2 h-2 rounded-full bg-[#12b76a] animate-pulse"></span>
        </div>

        {/* User Account / Logout */}
        {user ? (
          <div className="flex items-center gap-2">
            <div className="hidden md:flex items-center gap-1.5 px-3 py-1.5 bg-emerald-50 text-[#006d3c] rounded-2xl text-xs font-bold border border-emerald-200">
              <User className="w-3.5 h-3.5 text-[#006d3c]" />
              <span>{user.username}</span>
            </div>
            <button
              onClick={handleLogout}
              className="px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-700 rounded-2xl text-xs font-extrabold flex items-center gap-1.5 border border-red-200 transition-all cursor-pointer"
            >
              <LogOut className="w-3.5 h-3.5" />
              <span>Salir</span>
            </button>
          </div>
        ) : null}

        {/* Quick Sale Action Button */}
        <button
          onClick={() => navigate('/pos')}
          className="px-4 py-2 bg-[#006d3c] hover:bg-[#00522c] text-white rounded-2xl font-bold text-xs flex items-center gap-2 shadow-md shadow-[#006d3c]/20 active:scale-95 transition-all cursor-pointer"
        >
          <PlusCircle className="w-4 h-4" />
          <span>Nueva Venta</span>
        </button>
      </div>
    </header>
  );
}
