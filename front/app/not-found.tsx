import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center px-4">
      <div className="bg-[#111318] rounded-xl border border-white/5 p-10 max-w-md w-full text-center">
        <div className="text-6xl font-mono font-bold text-white/10 mb-4">404</div>
        <h1 className="text-xl font-semibold text-white mb-2">Página no encontrada</h1>
        <p className="text-white/50 text-sm mb-6">
          La ruta que intentas acceder no existe en el panel de inteligencia.
        </p>
        <Link
          href="/"
          className="inline-block px-5 py-2.5 bg-emerald-500/10 text-emerald-400 rounded-lg text-sm font-medium hover:bg-emerald-500/20 transition-colors"
        >
          Volver al Dashboard
        </Link>
      </div>
    </div>
  );
}
