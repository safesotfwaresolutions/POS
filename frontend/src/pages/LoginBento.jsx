import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Zap, User, Lock, KeyRound, ArrowRight, ShieldCheck, AlertCircle } from 'lucide-react';

export default function LoginBento() {
  const { login, loading } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Password123');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await login(username, password);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Error de autenticación. Verifica las credenciales.');
    }
  };

  const handleUseDemoAdmin = () => {
    setUsername('admin');
    setPassword('Password123');
  };

  return (
    <div className="min-h-screen bg-[#101828] text-white flex items-center justify-center p-4 relative overflow-hidden select-none">
      {/* Ambient Glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-blue-600/20 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-indigo-600/20 rounded-full blur-3xl pointer-events-none"></div>

      <div className="max-w-md w-full bento-card bg-[#162032] border-[#253247] p-8 rounded-3xl shadow-2xl space-y-6 relative z-10">
        
        {/* Header */}
        <div className="text-center space-y-2">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-blue-600 to-blue-400 flex items-center justify-center mx-auto shadow-lg shadow-blue-500/30">
            <Zap className="w-8 h-8 fill-white text-white" />
          </div>
          <h1 className="text-2xl font-black tracking-tight text-white mt-3">
            ProPOS <span className="text-blue-400 font-bold">Bento</span>
          </h1>
          <p className="text-xs text-gray-400 font-semibold">
            Inicia sesión en el Backend para acceder al Punto de Venta
          </p>
        </div>

        {error && (
          <div className="p-3.5 bg-red-500/10 border border-red-500/30 rounded-xl text-xs font-bold text-red-400 flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1">
            <label className="text-xs font-bold text-gray-300 block">Usuario:</label>
            <div className="relative">
              <User className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              <input
                type="text"
                value={username}
                onChange={e => setUsername(e.target.value)}
                placeholder="Ingresa usuario (ej. admin)..."
                className="w-full pl-10 pr-4 py-2.5 bg-[#0f172a] border border-[#253247] rounded-xl text-xs font-semibold text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-all"
                required
              />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-bold text-gray-300 block">Contraseña:</label>
            <div className="relative">
              <KeyRound className="w-4 h-4 text-gray-400 absolute left-3.5 top-3" />
              <input
                type="password"
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full pl-10 pr-4 py-2.5 bg-[#0f172a] border border-[#253247] rounded-xl text-xs font-semibold text-white placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-all"
                required
              />
            </div>
          </div>

          {/* Preset Admin Credentials Box */}
          <div className="p-3.5 bg-blue-500/10 rounded-2xl border border-blue-500/20 text-xs space-y-1.5">
            <div className="flex items-center justify-between">
              <span className="font-extrabold text-blue-300 flex items-center gap-1.5">
                <ShieldCheck className="w-4 h-4 text-blue-400" /> Credenciales Semilla por Defecto:
              </span>
              <button
                type="button"
                onClick={handleUseDemoAdmin}
                className="text-[10px] bg-blue-600 hover:bg-blue-500 text-white font-extrabold px-2 py-0.5 rounded-lg transition-colors"
              >
                Autocompletar
              </button>
            </div>
            <p className="text-[11px] text-gray-300">
              • Usuario: <code className="text-blue-300 font-bold font-mono">admin</code> | Contraseña: <code className="text-blue-300 font-bold font-mono">Password123</code>
            </p>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white font-black text-xs rounded-xl flex items-center justify-center gap-2 shadow-lg shadow-blue-600/30 active:scale-98 transition-all"
          >
            <span>{loading ? 'Autenticando en API REST...' : 'Ingresar al Sistema'}</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </form>

        <p className="text-[11px] text-center text-gray-500 font-medium">
          Conexión segura vía Spring Boot Security + JWT Bearer Token
        </p>

      </div>
    </div>
  );
}
