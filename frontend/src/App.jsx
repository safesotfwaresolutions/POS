import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';

import Sidebar from './components/layout/Sidebar';
import Header from './components/layout/Header';
import BackofficeSidebar from './components/backoffice/Sidebar';
import BackofficeHeader from './components/backoffice/Header';

import LoginBento from './pages/LoginBento';
import DashboardBento from './pages/DashboardBento';
import POSBento from './pages/POSBento';
import InventoryBento from './pages/InventoryBento';
import ProductsBento from './pages/ProductsBento';
import CustomersBento from './pages/CustomersBento';
import InvoicingBento from './pages/InvoicingBento';
import SettingsBento from './pages/SettingsBento';
import StoresBento from './pages/backoffice/StoresBento';
import CategoriesBento from './pages/backoffice/CategoriesBento';
import LegalDocumentsBento from './pages/backoffice/LegalDocumentsBento';
import SupportTicketsBento from './pages/backoffice/SupportTicketsBento';

function ProtectedLayout() {
  const { isAuthenticated, user } = useAuth();
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => {
    return localStorage.getItem('pos_sidebar_collapsed') === 'true';
  });

  const toggleSidebarCollapse = () => {
    setSidebarCollapsed(prev => {
      const next = !prev;
      localStorage.setItem('pos_sidebar_collapsed', next ? 'true' : 'false');
      return next;
    });
  };

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role === 'SUPER_ADMIN') {
    return <Navigate to="/backoffice" replace />;
  }

  return (
    <div className="min-h-screen bg-[#f7f9fc] dark:bg-[#0f172a] text-[#191c1e] dark:text-gray-100 flex transition-colors duration-300">
      {/* Expandable / Collapsible Sidebar */}
      <Sidebar
        isOpen={mobileSidebarOpen}
        onClose={() => setMobileSidebarOpen(false)}
        isCollapsed={sidebarCollapsed}
        onToggleCollapse={toggleSidebarCollapse}
      />

      {/* Main Workspace Area with Dynamic Padding (lg:pl-20 or lg:pl-64) */}
      <div className={`pl-0 transition-all duration-300 flex-1 flex flex-col min-w-0 ${
        sidebarCollapsed ? 'lg:pl-20' : 'lg:pl-64'
      }`}>
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

function BackofficeProtectedLayout() {
  const { isAuthenticated, user } = useAuth();
  const [mobileSidebarOpen, setMobileSidebarOpen] = useState(false);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(() => {
    return localStorage.getItem('pos_backoffice_sidebar_collapsed') === 'true';
  });

  const toggleSidebarCollapse = () => {
    setSidebarCollapsed(prev => {
      const next = !prev;
      localStorage.setItem('pos_backoffice_sidebar_collapsed', next ? 'true' : 'false');
      return next;
    });
  };

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (user?.role !== 'SUPER_ADMIN') {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="min-h-screen bg-[#f7f9fc] dark:bg-[#0f172a] text-[#191c1e] dark:text-gray-100 flex transition-colors duration-300">
      <BackofficeSidebar
        isOpen={mobileSidebarOpen}
        onClose={() => setMobileSidebarOpen(false)}
        isCollapsed={sidebarCollapsed}
        onToggleCollapse={toggleSidebarCollapse}
      />

      <div className={`pl-0 transition-all duration-300 flex-1 flex flex-col min-w-0 ${
        sidebarCollapsed ? 'lg:pl-20' : 'lg:pl-64'
      }`}>
        <BackofficeHeader
          onToggleMobileMenu={() => setMobileSidebarOpen(prev => !prev)}
        />

        <main className="p-4 sm:p-6 lg:p-8 flex-1 overflow-x-hidden">
          <Routes>
            <Route path="/" element={<StoresBento />} />
            <Route path="/categories" element={<CategoriesBento />} />
            <Route path="/legal-documents" element={<LegalDocumentsBento />} />
            <Route path="/support" element={<SupportTicketsBento />} />
            <Route path="*" element={<Navigate to="/backoffice" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<LoginBento />} />
            <Route path="/backoffice/*" element={<BackofficeProtectedLayout />} />
            <Route path="/*" element={<ProtectedLayout />} />
          </Routes>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  );
}
