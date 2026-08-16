import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';

import DashboardBento from './pages/DashboardBento';
import POSBento from './pages/POSBento';
import InventoryBento from './pages/InventoryBento';
import ProductsBento from './pages/ProductsBento';
import CustomersBento from './pages/CustomersBento';
import InvoicingBento from './pages/InvoicingBento';
import SettingsBento from './pages/SettingsBento';

export default function App() {
  const [activeRole, setActiveRole] = useState('Vendedor');

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-[#f5f6f8] text-[#111c2d] flex">
        {/* Fixed Left Sidebar (248px) */}
        <Sidebar activeRole={activeRole} />

        {/* Main Application Right Content Workspace */}
        <div className="pl-[248px] flex-1 flex flex-col min-w-0">
          <Header 
            activeRole={activeRole} 
            setActiveRole={setActiveRole} 
          />

          <main className="p-6 flex-1 overflow-x-hidden">
            <Routes>
              <Route path="/" element={<DashboardBento activeRole={activeRole} />} />
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
    </BrowserRouter>
  );
}
