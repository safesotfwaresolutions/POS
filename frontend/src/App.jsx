import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';

import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';

import LoginBento from './pages/LoginBento';
import DashboardBento from './pages/DashboardBento';
import POSBento from './pages/POSBento';
import InventoryBento from './pages/InventoryBento';
import ProductsBento from './pages/ProductsBento';
import CustomersBento from './pages/CustomersBento';
import InvoicingBento from './pages/InvoicingBento';
import SettingsBento from './pages/SettingsBento';

function ProtectedLayout() {
  const { isAuthenticated } = useAuth();
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="min-h-screen bg-[#f7f9fc] text-[#191c1e] flex">
      {/* Icon Sidebar matching Stitch MCP Screenshot (w-20 on Desktop) */}
      <Sidebar 
        isOpen={mobileSidebarOpen} 
        onClose={() => setMobileSidebarOpen(false)} 
      />

      {/* Main Workspace Area with pl-0 lg:pl-20 */}
      <div className="pl-0 lg:pl-20 flex-1 flex flex-col min-w-0 transition-all duration-300">
        <Header 
          onToggleMobileMenu={() => setMobileSidebarOpen(prev => !prev)} 
        />

        <main className="p-4 sm:p-6 lg:p-8 flex-1 overflow-x-hidden">
          <Routes>
            <Route path="/" element={<DashboardBento />} />
            <Route path="/pos" element={<POSBento />} />
            <Route path="/inventory" element={<InventoryBento />} />
            <Route path="/products" element={<ProductsBento />} />
            <Route path="/customers" element={<CustomersBento />} />
            <Route path="/invoicing" element={<InvoicingBento />} />
            <Route path="/settings" element={<SettingsBento />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginBento />} />
          <Route path="/*" element={<ProtectedLayout />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
