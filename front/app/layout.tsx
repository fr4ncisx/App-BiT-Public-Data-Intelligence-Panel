import "./globals.css";
import { Inter, JetBrains_Mono } from "next/font/google";
import React from "react";
import Providers from "../components/Providers";

const inter = Inter({
  subsets: ["latin"],
  variable: "--font-sans",
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ["latin"],
  variable: "--font-mono",
});

export const metadata = {
  title: "BiT.Intelligence - Florianópolis Territorial Gaps",
  description: "Plataforma de análisis de evidencia y brechas territoriales conectada a su backend externo",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es" className={`${inter.variable} ${jetbrainsMono.variable}`}>
      <body className="bg-[#0A0A0A] text-white font-sans antialiased">
        <Providers>
          {children}
        </Providers>
      </body>
    </html>
  );
}

