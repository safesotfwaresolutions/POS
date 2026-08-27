import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Zap, CheckCircle2, XCircle, Loader2 } from 'lucide-react';
import { verifyEmailApi } from '../services/authApi';
import { useAuth } from '../context/AuthContext';

export default function VerifyEmailBento() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { setSessionFromResponse } = useAuth();
  const [status, setStatus] = useState('verifying'); // verifying | success | error
  const [error, setError] = useState('');
  const attempted = useRef(false);

  useEffect(() => {
    if (attempted.current) return;
    attempted.current = true;

    const token = searchParams.get('token');
    if (!token) {
      setStatus('error');
      setError('Enlace de verificación inválido.');
      return;
    }

    (async () => {
      try {
        const data = await verifyEmailApi(token);
        setSessionFromResponse(data);
        setStatus('success');
        setTimeout(() => navigate('/onboarding/store', { replace: true }), 1200);
      } catch (err) {
        setStatus('error');
        setError(err.message || 'No se pudo verificar tu correo.');
      }
    })();
  }, [searchParams, navigate, setSessionFromResponse]);

  return (
    <div className="min-h-screen bg-[#101e19] text-white flex items-center justify-center p-4 relative overflow-hidden select-none">
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#006d3c]/30 rounded-full blur-3xl pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#12b76a]/20 rounded-full blur-3xl pointer-events-none"></div>

      <div className="max-w-md w-full bento-card bg-[#14231e] border-[#1d332c] p-8 rounded-3xl shadow-2xl space-y-6 relative z-10 text-center">
        <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-[#006d3c] to-[#12b76a] flex items-center justify-center mx-auto shadow-lg shadow-[#006d3c]/40">
          <Zap className="w-8 h-8 fill-white text-white" />
        </div>

        {status === 'verifying' && (
          <>
            <Loader2 className="w-8 h-8 text-[#12b76a] mx-auto animate-spin" />
            <p className="text-sm font-bold text-white">Verificando tu correo...</p>
          </>
        )}

        {status === 'success' && (
          <>
            <CheckCircle2 className="w-10 h-10 text-[#12b76a] mx-auto" />
            <p className="text-sm font-bold text-white">¡Correo verificado!</p>
            <p className="text-xs text-gray-300">Te llevamos a crear tu local...</p>
          </>
        )}

        {status === 'error' && (
          <>
            <XCircle className="w-10 h-10 text-red-400 mx-auto" />
            <p className="text-sm font-bold text-white">No pudimos verificar tu correo</p>
            <p className="text-xs text-gray-300">{error}</p>
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="text-xs font-extrabold text-[#12b76a] hover:underline cursor-pointer"
            >
              Volver a iniciar sesión
            </button>
          </>
        )}
      </div>
    </div>
  );
}
