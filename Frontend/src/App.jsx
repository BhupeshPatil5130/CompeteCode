import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Footer from './components/Footer';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import Problems from './pages/Problems';
import ProblemIDE from './pages/ProblemIDE';
import Sheets from './pages/Sheets';
import Roadmaps from './pages/Roadmaps';
import RoadmapDetail from './pages/RoadmapDetail';
import BattleGround from './pages/BattleGround';
import BattleRoom from './pages/BattleRoom';
import BattleHistory from './pages/BattleHistory';
import SpeedMode from './pages/SpeedMode';
import Leaderboard from './pages/Leaderboard';
import Profile from './pages/Profile';
import SubmissionHistory from './pages/SubmissionHistory';
import StoryMode from './pages/StoryMode';

function AppRoutes() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/problems" element={<Problems />} />
        <Route path="/problems/:id" element={<ProblemIDE />} />
        <Route path="/sheets" element={<Sheets />} />
        <Route path="/roadmaps" element={<Roadmaps />} />
        <Route path="/roadmaps/:id" element={<RoadmapDetail />} />
        <Route path="/leaderboard" element={<Leaderboard />} />
        <Route path="/profile" element={<Profile />} />
        <Route path="/submissions" element={<SubmissionHistory />} />
        <Route path="/story-mode" element={<StoryMode />} />
        <Route path="/speed-mode" element={<SpeedMode />} />
        <Route path="/battle-history" element={
          <ProtectedRoute><BattleHistory /></ProtectedRoute>
        } />
        <Route path="/battleground" element={<BattleGround />} />
        <Route path="/battleground/:id" element={<BattleRoom />} />
      </Routes>
    
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
        <Toaster
          position="bottom-right"
          toastOptions={{
            duration: 4000,
            style: {
              background: '#1e1e1e',
              color: '#fafafa',
              border: '1px solid #333',
              borderRadius: '8px',
              fontSize: '14px',
              padding: '12px 16px',
            },
            success: {
              iconTheme: { primary: '#10b981', secondary: '#0c0c0c' },
            },
            error: {
              iconTheme: { primary: '#ef4444', secondary: '#0c0c0c' },
            },
          }}
        />
      </AuthProvider>
    </BrowserRouter>
  );
}
