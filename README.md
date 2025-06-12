# Boids Simulation in Scala

This is a basic implementation of a Boids simulation (flocking behavior) written in Scala using a voxel-based spatial grid and concurrency. It uses core Scala for the simulation logic and ScalaFX for rendering the visuals. 

The goal was to explore how simple rules can lead to complex, emergent behavior — and to get more comfortable with Scala and GUI development.

The base fluid performance is 1000 boids, but can pretty much handle up to 10k entities, provided you lessen the boid size and maybe the detection ranges.
The voxel grid scales to the biggest range (detection, hunting or separation)

## Features

- Real-time simulation of boid flocking behavior
- Adjustable parameters for speed, perception radius, etc.
- Simple ScalaFX GUI for visualization

## Requirements

- Scala (version 3.3.x or compatible)
- sbt (Scala Build Tool)

## How to Run

1. Clone the repository:

```bash
git clone https://github.com/jakubchlapek/boids.git
cd boids
```

2. Run with sbt:

```bash
sbt run
```

## Screenshots

![Image](https://github.com/user-attachments/assets/c25b3141-6691-4cb8-a900-cd8eb9ce279f)

## TODO

- Akka-based implementation for better concurrency and scalability

## Acknowledgments

- Inspired by Craig Reynolds' original Boids model
- Built as a fun side project for learning purposes

## License

MIT

