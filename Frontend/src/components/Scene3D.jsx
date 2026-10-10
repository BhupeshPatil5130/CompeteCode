import React, { useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { Float, Sphere, Torus, Octahedron, Environment } from '@react-three/drei';

function FloatingShapes() {
  const groupRef = useRef();

  // Subtle rotation of the entire group based on mouse position
  useFrame((state) => {
    const t = state.clock.getElapsedTime();
    groupRef.current.rotation.y = Math.sin(t / 4) * 0.2 + (state.pointer.x * 0.1);
    groupRef.current.rotation.x = Math.cos(t / 4) * 0.2 + (-state.pointer.y * 0.1);
  });

  return (
    <group ref={groupRef}>
      <Float speed={1.5} rotationIntensity={1.5} floatIntensity={2}>
        <Torus args={[1.5, 0.4, 16, 32]} position={[-3, 1, -2]} rotation={[Math.PI / 4, 0, 0]}>
          <meshStandardMaterial color="#f59e0b" roughness={0.2} metalness={0.8} />
        </Torus>
      </Float>

      <Float speed={2} rotationIntensity={2} floatIntensity={1.5}>
        <Sphere args={[0.8, 32, 32]} position={[3, -1.5, -1]}>
          <meshStandardMaterial color="#10b981" roughness={0.1} metalness={0.5} />
        </Sphere>
      </Float>

      <Float speed={1} rotationIntensity={1} floatIntensity={3}>
        <Octahedron args={[1]} position={[2, 2.5, -3]} rotation={[0, Math.PI / 4, 0]}>
          <meshStandardMaterial color="#8b5cf6" roughness={0.3} metalness={0.6} wireframe />
        </Octahedron>
      </Float>

      <Float speed={1.8} rotationIntensity={1.2} floatIntensity={2}>
        <Sphere args={[0.4, 32, 32]} position={[-4, -2, 1]}>
          <meshStandardMaterial color="#3b82f6" roughness={0.2} metalness={0.7} />
        </Sphere>
      </Float>
    </group>
  );
}

export default function Scene3D() {
  return (
    <div className="absolute inset-0 z-0 pointer-events-none opacity-50 overflow-hidden">
      <Canvas camera={{ position: [0, 0, 8], fov: 45 }}>
        <ambientLight intensity={0.5} />
        <directionalLight position={[10, 10, 5]} intensity={1.5} color="#ffffff" />
        <directionalLight position={[-10, -10, -5]} intensity={0.5} color="#f59e0b" />
        
        <FloatingShapes />
        
        {/* Adds realistic reflections */}
        <Environment preset="city" />
      </Canvas>
    </div>
  );
}
