import React, { useEffect, useRef } from 'react'
import * as THREE from 'three'

// A real npm module (three.js) used inside a SpringReact custom widget. The Java side
// renders it with `widget("Cube", attr("color", "#6d8bff"))`; props arrive here, plus a
// `call(action, ...)` to invoke server actions.
type CubeProps = { color?: string; size?: number; call?: (a: string, ...x: unknown[]) => void }

function Cube({ color = '#6d8bff', size = 160 }: CubeProps) {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const el = ref.current
    if (!el) return

    const scene = new THREE.Scene()
    const camera = new THREE.PerspectiveCamera(60, 1, 0.1, 100)
    camera.position.z = 3

    const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
    renderer.setSize(size, size)
    el.appendChild(renderer.domElement)

    const cube = new THREE.Mesh(
      new THREE.BoxGeometry(1.4, 1.4, 1.4),
      new THREE.MeshStandardMaterial({ color, roughness: 0.35, metalness: 0.4 }),
    )
    scene.add(cube)
    scene.add(new THREE.AmbientLight(0xffffff, 0.6))
    const light = new THREE.DirectionalLight(0xffffff, 1.1)
    light.position.set(3, 4, 5)
    scene.add(light)

    let raf = 0
    const tick = () => {
      cube.rotation.x += 0.012
      cube.rotation.y += 0.016
      renderer.render(scene, camera)
      raf = requestAnimationFrame(tick)
    }
    tick()

    return () => {
      cancelAnimationFrame(raf)
      renderer.dispose()
      el.removeChild(renderer.domElement)
    }
  }, [color, size])

  return <div ref={ref} style={{ width: size, height: size, margin: '0 auto' }} />
}

// Register the widget so Java/Kotlin screens can embed it via widget("Cube", ...).
window.SpringReact.registerWidget('Cube', Cube)
