/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { motion } from 'motion/react';
import { CheckCircle2, Circle, Smartphone, Settings, Database, Navigation, Layout, PieChart, ShieldCheck } from 'lucide-react';

const phases = [
  { id: 1, name: 'Fundação do Projeto', status: 'completed', icon: Settings },
  { id: 2, name: 'Banco de Dados (Room)', status: 'completed', icon: Database },
  { id: 3, name: 'Navegação e Shell', status: 'completed', icon: Navigation },
  { id: 4, name: 'Módulo de Tarefas', status: 'completed', icon: CheckCircle2 },
  { id: 5, name: 'Módulo Financeiro', status: 'completed', icon: Smartphone },
  { id: 6, name: 'Módulo de Dívidas', status: 'completed', icon: Circle },
  { id: 7, name: 'Dashboard e Calendário', status: 'completed', icon: Layout },
  { id: 8, name: 'Gamificação e Relatórios', status: 'completed', icon: PieChart },
  { id: 9, name: 'Backup e Polimento', status: 'completed', icon: ShieldCheck },
];

export default function App() {
  return (
    <div className="min-h-screen bg-slate-50 p-8 font-sans">
      <header className="max-w-4xl mx-auto mb-12 text-center">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="inline-block p-2 bg-indigo-100 rounded-2xl mb-4"
        >
          <Smartphone className="w-8 h-8 text-indigo-600" />
        </motion.div>
        <h1 className="text-4xl font-bold text-slate-900 mb-2">LifeFlow Pro</h1>
        <p className="text-slate-600 max-w-lg mx-auto">
          Painel de Desenvolvimento do Aplicativo Android Nativo.
          Acompanhe o progresso das fases do roadmap abaixo.
        </p>
      </header>

      <main className="max-w-4xl mx-auto grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {phases.map((phase) => (
          <motion.div
            key={phase.id}
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ delay: phase.id * 0.1 }}
            className={`p-6 rounded-2xl border ${
              phase.status === 'completed'
                ? 'bg-white border-green-200 shadow-sm'
                : 'bg-white border-slate-200 opacity-60'
            }`}
          >
            <div className="flex items-start justify-between mb-4">
              <div className={`p-3 rounded-xl ${
                phase.status === 'completed' ? 'bg-green-100 text-green-600' : 'bg-slate-100 text-slate-400'
              }`}>
                <phase.icon className="w-6 h-6" />
              </div>
              {phase.status === 'completed' ? (
                <CheckCircle2 className="w-5 h-5 text-green-500" />
              ) : (
                <Circle className="w-5 h-5 text-slate-300" />
              )}
            </div>
            <h3 className="font-semibold text-slate-900 mb-1">Fase {phase.id}</h3>
            <p className="text-slate-700 font-medium">{phase.name}</p>
            <div className="mt-4 flex items-center gap-2">
               <div className={`h-1.5 flex-1 rounded-full ${
                 phase.status === 'completed' ? 'bg-green-100' : 'bg-slate-100'
               }`}>
                 <div className={`h-full rounded-full ${
                   phase.status === 'completed' ? 'bg-green-500 w-full' : 'bg-slate-300 w-0'
                 }`} />
               </div>
               <span className="text-xs text-slate-500 font-mono">
                 {phase.status === 'completed' ? '100%' : '0%'}
               </span>
            </div>
          </motion.div>
        ))}
      </main>

      <footer className="max-w-4xl mx-auto mt-16 pt-8 border-t border-slate-200 text-center text-slate-500 text-sm">
        <p>Projeto Android com Kotlin, Jetpack Compose e Hilt.</p>
        <p className="mt-1 font-mono">Package: com.lifeflowpro.app</p>
      </footer>
    </div>
  );
}
