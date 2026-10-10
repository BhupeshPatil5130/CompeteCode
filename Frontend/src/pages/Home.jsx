import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Code2, Swords, Map, Sparkles, Zap, Trophy, ArrowRight, LayoutGrid, Bot, Terminal } from 'lucide-react';
import Scene3D from '../components/Scene3D';
import StatsCard from '../components/StatsCard';
import DailyChallenge from '../components/DailyChallenge';
import Footer from '../components/Footer';

const features = [
  { icon: Code2, title: 'Online IDE', desc: 'Monaco-powered editor with Java, Python, C++, and JavaScript support.' },
  { icon: Bot, title: 'AI Test Checker', desc: 'Gemini generates hidden test cases and evaluates your code in real-time.' },
  { icon: LayoutGrid, title: '8 DSA Sheets', desc: '50+ curated problems across Arrays, Trees, Graphs, DP and more.' },
  { icon: Map, title: 'Learning Roadmaps', desc: 'Structured paths from basics to advanced with progress tracking.' },
  { icon: Swords, title: 'Live Battles', desc: 'Compete head-to-head — highest score wins ELO rating points.' },
  { icon: Sparkles, title: 'Visual Explanations', desc: 'Animated step-by-step walkthroughs for every algorithm.' },
];

const steps = [
  { num: '01', title: 'Pick a Problem', desc: 'Choose from 50+ problems across 8 DSA topics, organized by difficulty.' },
  { num: '02', title: 'Write Your Code', desc: 'Use the built-in Monaco editor with syntax highlighting and auto-complete.' },
  { num: '03', title: 'Submit & Learn', desc: 'AI generates test cases, validates your solution, and gives feedback.' },
];

export default function Home() {
  return (
    <div className="flex flex-col min-h-[calc(100vh-3.5rem)] relative">
      {/* 3D Background */}
      <Scene3D />

      {/* Hero Section */}
      <div className="relative pt-32 pb-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto w-full flex-1 flex flex-col justify-center">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[800px] h-[600px] bg-amber-500/[0.03] rounded-full blur-[120px]" />
        <div className="absolute bottom-0 right-1/4 w-[400px] h-[400px] bg-purple-500/[0.03] rounded-full blur-[100px]" />
        
        {/* ══ HERO ══ */}
        <section className="relative max-w-5xl mx-auto px-4 pt-20 pb-16 text-center">
          <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-amber-500/10 border border-amber-500/20 text-amber-400 text-xs font-semibold mb-6 tracking-wide uppercase">
              <Zap className="w-3 h-3" /> 65+ Problems · 10 DSA Sheets · Live Battles
            </div>

            <h1 className="text-4xl sm:text-5xl md:text-6xl font-extrabold text-white mb-5 tracking-tight leading-[1.1]">
              Master DSA the
              <br />
              <span className="gradient-text">smart way.</span>
            </h1>

            <p className="text-lg text-neutral-400 max-w-xl mx-auto mb-8 leading-relaxed">
              AI-powered test validation, animated explanations, live coding battles, and structured roadmaps — everything you need to crack coding interviews.
            </p>

            <div className="flex gap-3 justify-center flex-wrap">
              <Link
                to="/problems"
                className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-semibold text-sm transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-amber-500/20"
              >
                Start Solving <ArrowRight className="w-4 h-4" />
              </Link>
              <Link
                to="/sheets"
                className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg border border-neutral-700 hover:border-neutral-600 text-white font-medium text-sm transition-all hover:bg-surface-hover"
              >
                Browse Sheets
              </Link>
            </div>
          </motion.div>

          {/* Code snippet preview */}
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="mt-14 max-w-2xl mx-auto"
          >
            <div className="bg-surface-secondary border border-border rounded-xl overflow-hidden shadow-2xl shadow-black/40">
              <div className="flex items-center gap-1.5 px-4 py-2.5 border-b border-border bg-surface-tertiary">
                <div className="w-3 h-3 rounded-full bg-red-500/60" />
                <div className="w-3 h-3 rounded-full bg-yellow-500/60" />
                <div className="w-3 h-3 rounded-full bg-green-500/60" />
                <span className="ml-3 text-xs text-neutral-500 font-mono">Solution.java</span>
              </div>
              <pre className="p-5 text-sm font-mono text-left text-neutral-300 leading-relaxed overflow-x-auto">
                <code>
                  <span className="text-purple-400">class</span> <span className="text-amber-300">Solution</span> {'{\n'}
                  {'  '}<span className="text-purple-400">public int</span>[] <span className="text-blue-400">twoSum</span>(<span className="text-purple-400">int</span>[] nums, <span className="text-purple-400">int</span> target) {'{\n'}
                  {'    '}<span className="text-neutral-500">// HashMap approach — O(n)</span>{'\n'}
                  {'    '}<span className="text-purple-400">Map</span>{'<Integer, Integer>'} map = <span className="text-purple-400">new</span> <span className="text-amber-300">HashMap</span>{'<>();\n'}
                  {'    '}<span className="text-purple-400">for</span> (<span className="text-purple-400">int</span> i = <span className="text-emerald-400">0</span>; i {'<'} nums.length; i++) {'{\n'}
                  {'      '}<span className="text-purple-400">int</span> complement = target - nums[i];{'\n'}
                  {'      '}<span className="text-purple-400">if</span> (map.containsKey(complement)){'\n'}
                  {'        '}<span className="text-purple-400">return new int</span>[]{'{'}<span className="text-neutral-400">map.get(complement), i</span>{'};'}{'\n'}
                  {'      '}map.put(nums[i], i);{'\n'}
                  {'    }\n'}
                  {'    '}<span className="text-purple-400">return new int</span>[]{'{};\n'}
                  {'  }\n'}
                  {'}'}
                </code>
              </pre>
            </div>
          </motion.div>
        </section>
      </div>

      {/* ══ DAILY CHALLENGE ══ */}
      <section className="max-w-5xl mx-auto px-4 py-6">
        <DailyChallenge />
      </section>

      {/* ══ STATS ══ */}
      <section className="max-w-5xl mx-auto px-4 py-12">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <StatsCard icon={Code2} label="Problems" value="65" suffix="+" delay={0} />
          <StatsCard icon={LayoutGrid} label="DSA Sheets" value="10" delay={100} />
          <StatsCard icon={Swords} label="Battle Mode" value="1" suffix="v1" delay={200} />
          <StatsCard icon={Trophy} label="Languages" value="4" delay={300} />
        </div>
      </section>

      {/* ══ FEATURES ══ */}
      <section className="max-w-5xl mx-auto px-4 py-16">
        <div className="text-center mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold text-white mb-3">Everything you need to level up</h2>
          <p className="text-neutral-500 max-w-lg mx-auto">A complete platform for practicing DSA, competing, and learning with AI assistance.</p>
        </div>

        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {features.map(({ icon: Icon, title, desc }, i) => (
            <motion.div
              key={title}
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.07 }}
              className="bg-surface-secondary border border-border rounded-xl p-5 hover:border-neutral-700 transition-colors group"
            >
              <div className="w-10 h-10 rounded-lg bg-amber-500/10 flex items-center justify-center mb-4 group-hover:bg-amber-500/15 transition-colors">
                <Icon className="w-5 h-5 text-amber-400" />
              </div>
              <h3 className="text-sm font-semibold text-white mb-1.5">{title}</h3>
              <p className="text-sm text-neutral-500 leading-relaxed">{desc}</p>
            </motion.div>
          ))}
        </div>
      </section>

      {/* ══ HOW IT WORKS ══ */}
      <section className="max-w-5xl mx-auto px-4 py-16">
        <div className="text-center mb-12">
          <h2 className="text-2xl sm:text-3xl font-bold text-white mb-3">How it works</h2>
          <p className="text-neutral-500">Three steps to start mastering algorithms.</p>
        </div>

        <div className="grid md:grid-cols-3 gap-6">
          {steps.map(({ num, title, desc }, i) => (
            <motion.div
              key={num}
              initial={{ opacity: 0, y: 16 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ delay: i * 0.1 }}
              className="relative"
            >
              <div className="text-5xl font-extrabold text-neutral-800 mb-3 font-mono">{num}</div>
              <h3 className="text-lg font-semibold text-white mb-2">{title}</h3>
              <p className="text-sm text-neutral-500 leading-relaxed">{desc}</p>
            </motion.div>
          ))}
        </div>

        <div className="text-center mt-12">
          <Link
            to="/register"
            className="inline-flex items-center gap-2 px-6 py-2.5 rounded-lg bg-amber-500 hover:bg-amber-600 text-black font-semibold text-sm transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-amber-500/20"
          >
            Create Free Account <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </section>

      <Footer />
    </div>
  );
}
