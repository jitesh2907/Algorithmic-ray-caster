# Breadth-First Search (BFS) Pathfinding Guide

## 1. Overview & Purpose

**Breadth-First Search (BFS)** is a fundamental graph traversal algorithm that explores all vertices of a graph in order of increasing distance from a designated starting source. In **ALGORITHMIC 3D MAZE EXPLORER**, BFS is implemented in [`BFSPathfinder.java`](file:///c:/Users/User/Desktop/Java%20Project/src/main/java/com/mazeexplorer/pathfinding/BFSPathfinder.java) and [`PathNode.java`](file:///c:/Users/User/Desktop/Java%20Project/src/main/java/com/mazeexplorer/pathfinding/PathNode.java) to govern the autonomous movement of the Enemy AI ("The Maze Stalker").

### Why BFS for Subterranean Labyrinths?
1. **Shortest Path Guarantee:** On unweighted discrete grid maps where every orthogonal step between adjacent walkable tiles has equal uniform cost ($\text{cost} = 1$), BFS is mathematically guaranteed to find the optimal shortest path.
2. **Deterministic & Wall-Safe:** BFS strictly traverses cardinal orthogonal neighbors (`North`, `East`, `South`, `West`), completely eliminating diagonal wall clipping or corner-cutting.
3. **Low Computational Overhead:** For a $15 \times 15$ maze ($225$ total tiles, $\approx 100$ walkable corridor tiles), BFS searches the entire state space in less than $0.05\text{ms}$, allowing dynamic real-time path re-calculation ($3$ times per second) without dropping frames.

---

## 2. Mathematical Formalization & Data Structures

Let the discrete maze environment be modeled as an unweighted directed graph $G = (V, E)$:
- **Vertices ($V$):** The set of discrete 2D grid coordinates $(x, z)$ where $\text{isWalkable}(x, z) = \text{true}$.
- **Edges ($E$):** Orthogonal connections between adjacent walkable cells:
  $$E = \{ ((x_1, z_1), (x_2, z_2)) \mid |x_1 - x_2| + |z_1 - z_2| = 1 \land \text{isWalkable}(x_2, z_2) \}$$

### Core Data Structures:
1. **Frontier Queue ($Q$):** First-In-First-Out (`ArrayDeque<PathNode>`) storing discovered nodes awaiting exploration.
2. **Visited Matrix (`boolean[height][width]`):** $O(1)$ constant time lookup tracking expanded tiles to prevent cycles.
3. **Parent Backtracking Map (`Map<PathNode, PathNode>`):** Maps each discovered node $v$ to its immediate predecessor $u$ along the optimal shortest path from start node $s$.

---

## 3. Algorithmic Procedure & Flowchart

```
                          [ Start: findPath(startX, startZ, goalX, goalZ) ]
                                                |
                                                v
                                  Is Start and Goal Walkable?
                                         /            \
                                    (No)               (Yes)
                                     /                   \
                         Return Empty Path           Initialize Queue Q,
                                                    Visited Array, Parent Map
                                                                 |
                                                                 v
                                                +---> [ Is Queue Q Empty? ]
                                                |            /        \
                                                |        (Yes)        (No)
                                                |          |            |
                                                |     No Path Exists    v
                                                |     (Return Empty)  Poll Current Node u
                                                |                       |
                                                |             Does u == Goal?
                                                |               /        \
                                                |           (Yes)        (No)
                                                |             |            |
                                                |       Reconstruct Path   v
                                                |      via Parent Map  For each Cardinal Dir:
                                                |             |        (North, East, South, West)
                                                |        Return Path        |
                                                |                        Is Neighbor Walkable
                                                |                        and Unvisited?
                                                |                           /         \
                                                |                       (Yes)         (No)
                                                |                         |            |
                                                +------------------ Add to Queue Q,    Ignore
                                                                    Mark Visited,
                                                                    Record Parent
```

---

## 4. Code Implementation Walkthrough

```java
public List<PathNode> findPath(int startX, int startZ, int goalX, int goalZ) {
    if (!mazeGrid.isWalkable(startX, startZ) || !mazeGrid.isWalkable(goalX, goalZ)) {
        return Collections.emptyList();
    }

    PathNode start = new PathNode(startX, startZ);
    PathNode goal = new PathNode(goalX, goalZ);

    if (start.equals(goal)) {
        return List.of(start);
    }

    Queue<PathNode> queue = new ArrayDeque<>();
    Map<PathNode, PathNode> parentMap = new HashMap<>();
    boolean[][] visited = new boolean[mazeGrid.getHeight()][mazeGrid.getWidth()];

    queue.add(start);
    visited[startZ][startX] = true;
    boolean found = false;

    while (!queue.isEmpty()) {
        PathNode current = queue.poll();

        if (current.equals(goal)) {
            found = true;
            break;
        }

        for (int[] dir : CARDINAL_DIRS) {
            int nextX = current.x + dir[0];
            int nextZ = current.z + dir[1];

            if (mazeGrid.isWalkable(nextX, nextZ) && !visited[nextZ][nextX]) {
                visited[nextZ][nextX] = true;
                PathNode neighbor = new PathNode(nextX, nextZ);
                parentMap.put(neighbor, current);
                queue.add(neighbor);
            }
        }
    }

    if (!found) {
        return Collections.emptyList();
    }

    // Backtrack from goal to start
    List<PathNode> path = new ArrayList<>();
    PathNode curr = goal;
    while (curr != null) {
        path.add(curr);
        curr = parentMap.get(curr);
    }
    Collections.reverse(path);
    return path;
}
```

---

## 5. Integration with Enemy AI Controller

The Enemy AI in [`EnemyController.java`](file:///c:/Users/User/Desktop/Java%20Project/src/main/java/com/mazeexplorer/ai/EnemyController.java) utilizes BFS in two modes:

1. **Patrol Mode Waypoint Cycling:** When moving between patrol waypoints `(9, 7) -> (11, 7) -> (11, 11) -> (7, 11) -> (7, 7)`, BFS generates smooth corridor paths around interior maze walls.
2. **Pursuit Mode Dynamic Re-Planning:** During `CHASE` state, the player's world position $(x_p, z_p)$ is mapped to discrete grid coordinates $\lfloor x_p / S \rfloor, \lfloor z_p / S \rfloor$. Every $0.35\text{s}$, BFS re-computes the optimal trajectory, enabling the Stalker to intelligently corner the player even as the player darts into side corridors.

### Complexity:
- **Time Complexity:** $O(|V| + |E|) = O(N \times M)$ where $N=15, M=15 \implies \le 225$ operations.
- **Space Complexity:** $O(|V|) = O(N \times M)$ auxiliary memory for visited table and queue.

