# Core Algorithmic Foundations & Mathematical Principles

This document provides a comprehensive technical breakdown of the mathematics, algorithms, and geometric models driving the **Algorithmic ASCII-Art Ray Caster**.

---

## 1. Coordinate Space & Orientation Geometry

The virtual maze is modeled on a Cartesian discrete 2D grid:
- Map dimensions: $W_{\text{map}} \times H_{\text{map}}$ grid cells.
- Grid coordinate system:
  - $X \in [0, W_{\text{map}})$, where $+X$ extends to the **East**.
  - $Y \in [0, H_{\text{map}})$, where $+Y$ extends to the **South**.
  - $Y=0$ corresponds to the Northern boundary, and $X=0$ corresponds to the Western boundary.

### Player Vectors
The player position is represented as continuous floating-point coordinates $(p_x, p_y) \in \mathbb{R}^2$.
The viewing direction is defined by angle $\theta \in [0, 2\pi)$ measured in radians:

$$\vec{u} = \begin{bmatrix} \cos(\theta) \\ \sin(\theta) \end{bmatrix}$$

When the player moves forward by step size $s$:
$$\Delta x = s \cdot \cos(\theta), \quad \Delta y = s \cdot \sin(\theta)$$

When the player strafes perpendicular to the viewing angle:
$$\theta_{\text{strafe}} = \theta \pm \frac{\pi}{2}$$

Angle normalization ensures $\theta$ wraps continuously within $[0, 2\pi)$:
$$\theta_{\text{normalized}} = ((\theta \bmod 2\pi) + 2\pi) \bmod 2\pi$$

---

## 2. Camera Plane & Field-of-View (FOV) Formulation

To cast rays across a field of view of $\text{FOV} = 60^\circ$ ($\frac{\pi}{3}$ radians) across $W_{\text{screen}} = 80$ horizontal columns, we define the camera projection plane.

The camera projection plane vector $\vec{c}$ is perpendicular to the view direction vector $\vec{u}$:
$$\vec{c} = \begin{bmatrix} -\sin(\theta) \\ \cos(\theta) \end{bmatrix} \cdot \tan\left(\frac{\text{FOV}}{2}\right)$$

For each screen column index $x \in [0, W_{\text{screen}} - 1]$, the normalized camera coordinate $\alpha_x \in [-1.0, +1.0]$ is computed as:
$$\alpha_x = \frac{2x}{W_{\text{screen}}} - 1.0$$

The unnormalized ray direction vector $\vec{r}_x$ for column $x$ is:
$$\vec{r}_x = \vec{u} + \alpha_x \vec{c} = \begin{bmatrix} \cos(\theta) - \alpha_x \sin(\theta)\tan\left(\frac{\text{FOV}}{2}\right) \\ \sin(\theta) + \alpha_x \cos(\theta)\tan\left(\frac{\text{FOV}}{2}\right) \end{bmatrix}$$

The ray angle $\theta_{\text{ray}}$ is:
$$\theta_{\text{ray}} = \text{atan2}(r_{y}, r_{x})$$

---

## 3. Digital Differential Analyzer (DDA) Algorithm

The Digital Differential Analyzer (DDA) is an optimized algorithm for rasterizing lines through discrete grid cells. Instead of stepping by small arbitrary increments (which causes either over-sampling or tunneling through thin walls), DDA calculates the exact intersections with discrete grid lines.

```
       Y-Grid Lines
       |      |      |
----+------+------+------+----
    |      |      |      |
    |  *---+------+-->   |  <-- Ray stepping from cell to cell
    | /    |      |      |
----+------+------+------+---- X-Grid Lines
    |/     |      |      |
    P(px,py)      |  [WALL]
```

### 3.1 Delta Distances
The distance along the ray to traverse exactly 1 grid unit in the $X$ and $Y$ dimensions:
$$\Delta_{\text{dist}X} = \left| \frac{1}{r_x} \right|, \quad \Delta_{\text{dist}Y} = \left| \frac{1}{r_y} \right|$$

If $r_x = 0$ or $r_y = 0$, the corresponding $\Delta_{\text{dist}}$ is treated as $+\infty$ ($10^{30}$).

### 3.2 Step Direction & Initial Side Distances
Let $(m_x, m_y) = (\lfloor p_x \rfloor, \lfloor p_y \rfloor)$ be the integer grid cell containing the ray origin.
The initial distance to the nearest grid boundary is:

$$\text{step}_X = \begin{cases} -1 & \text{if } r_x < 0 \\ +1 & \text{if } r_x \ge 0 \end{cases}, \quad \text{sideDist}_X = \begin{cases} (p_x - m_x) \cdot \Delta_{\text{dist}X} & \text{if } r_x < 0 \\ (m_x + 1.0 - p_x) \cdot \Delta_{\text{dist}X} & \text{if } r_x \ge 0 \end{cases}$$

$$\text{step}_Y = \begin{cases} -1 & \text{if } r_y < 0 \\ +1 & \text{if } r_y \ge 0 \end{cases}, \quad \text{sideDist}_Y = \begin{cases} (p_y - m_y) \cdot \Delta_{\text{dist}Y} & \text{if } r_y < 0 \\ (m_y + 1.0 - p_y) \cdot \Delta_{\text{dist}Y} & \text{if } r_y \ge 0 \end{cases}$$

### 3.3 Traversal Loop
In each iteration, the algorithm jumps to the closest grid boundary:
```
while (!hit && steps < maxSteps):
    if (sideDist_X < sideDist_Y):
        sideDist_X += deltaDist_X
        m_x += step_X
        side = 0  // Vertical wall line intersected (East/West facing)
    else:
        sideDist_Y += deltaDist_Y
        m_y += step_Y
        side = 1  // Horizontal wall line intersected (North/South facing)
    
    if (map[m_y][m_x] > 0):
        hit = true
```

---

## 4. Wall Distance & Fish-Eye Distortion Correction

### Why Fish-Eye Distortion Occurs
If Euclidean distance $d_{\text{euclid}} = \sqrt{(x_{\text{hit}} - p_x)^2 + (y_{\text{hit}} - p_y)^2}$ were used directly for rendering, flat straight walls would appear curved/convex like looking through a fish-eye peephole lens.

This distortion occurs because rays cast at the outer edges of the screen travel a greater Euclidean distance to reach the wall than rays cast straight ahead through the center, even when looking at a perfectly straight planar wall!

```
               [ STRAIGHT FLAT WALL ]
               |         |          |
               |         |          |
               +---------+----------+
                \        |         /
                 \       |        /
      Ray (Far)   \   Perp|       / Ray (Far)
      Euclidean    \ Dist |      /  Euclidean
      is longer!    \    |     /   is longer!
                     \   |    /
                      \  |   /
                       \ |  /
                         P (Player)
```

### The Correction Formula
To project correctly onto a flat 2D viewport, the Euclidean distance must be projected onto the player's viewing normal:
$$d_{\text{perp}} = d_{\text{euclid}} \cdot \cos(\theta_{\text{ray}} - \theta_{\text{player}})$$

In our DDA implementation, the ray parameter $t$ to the wall intersection is:
$$t = \begin{cases} \frac{m_x - p_x + \frac{1 - \text{step}_X}{2}}{r_x} & \text{if } \text{side} = 0 \\ \frac{m_y - p_y + \frac{1 - \text{step}_Y}{2}}{r_y} & \text{if } \text{side} = 1 \end{cases}$$

The exact intersection point is:
$$x_{\text{hit}} = p_x + t \cdot r_x, \quad y_{\text{hit}} = p_y + t \cdot r_y$$
$$d_{\text{euclid}} = \sqrt{(x_{\text{hit}} - p_x)^2 + (y_{\text{hit}} - p_y)^2}$$
$$d_{\text{perp}} = (x_{\text{hit}} - p_x) \cos(\theta) + (y_{\text{hit}} - p_y) \sin(\theta) = d_{\text{euclid}} \cdot \cos(\theta_{\text{ray}} - \theta)$$

This mathematical projection guarantees that straight walls render with uniform perspective and straight horizontal edges.

---

## 5. Perspective Projection & Height Mapping

Given the perpendicular distance $d_{\text{perp}}$ and terminal screen height $H_{\text{screen}} = 24$ rows:
The apparent projected wall height $h_{\text{wall}}$ in screen character rows is inversely proportional to distance:

$$h_{\text{wall}} = \left\lfloor \frac{H_{\text{screen}}}{d_{\text{perp}}} \right\rfloor$$

The top row index $y_{\text{top}}$ and bottom row index $y_{\text{bottom}}$ on the screen are:
$$y_{\text{top}} = -\frac{h_{\text{wall}}}{2} + \frac{H_{\text{screen}}}{2}$$
$$y_{\text{bottom}} = \frac{h_{\text{wall}}}{2} + \frac{H_{\text{screen}}}{2}$$

These indices are clamped to valid screen bounds $[0, H_{\text{screen}} - 1]$:
$$\text{clamped}_{\text{start}} = \max(0, y_{\text{top}}), \quad \text{clamped}_{\text{end}} = \min(H_{\text{screen}} - 1, y_{\text{bottom}})$$

Screen rows are partitioned into three vertical regions:
1. **Ceiling:** For rows $y \in [0, \text{clamped}_{\text{start}} - 1]$
2. **Wall Slice:** For rows $y \in [\text{clamped}_{\text{start}}, \text{clamped}_{\text{end}}]$
3. **Floor:** For rows $y \in [\text{clamped}_{\text{end}} + 1, H_{\text{screen}} - 1]$

---

## 6. ASCII Shading Model

ASCII characters simulate light intensity and depth attenuation based on distance and surface orientation:

### Surface Orientation Contrast (Simulated Directional Lighting)
Vertical wall hits ($\text{side} = 0$, East/West surfaces) receive direct simulated sunlight, while horizontal wall hits ($\text{side} = 1$, North/South surfaces) receive indirect ambient shading.

| Distance Range | Side 0 (X-Axis Hit) | Side 1 (Y-Axis Hit) | Unicode Blocks | Perceived Brightness |
|---|---|---|---|---|
| $d < 1.5$ | `@` | `#` | `█` / `▓` | Very Bright / Immediate |
| $1.5 \le d < 3.0$ | `%` | `$` | `▓` / `▒` | Bright / Near |
| $3.0 \le d < 5.0$ | `*` | `+` | `▒` / `░` | Medium / Midground |
| $5.0 \le d < 8.0$ | `=` | `-` | `░` / `.` | Dim / Far |
| $d \ge 8.0$ | `:` | `.` | `.` / `.` | Very Dim / Horizon |

### Floor Depth Gradient
For a floor row $y > \frac{H_{\text{screen}}}{2}$, the normalized depth $\eta_y \in [0.0, 1.0]$ from the player's feet to the horizon is:
$$\eta_y = \frac{H_{\text{screen}} - 1 - y}{\frac{H_{\text{screen}}}{2}}$$
- $\eta_y < 0.25$ (closest to feet): `=` or `▒`
- $0.25 \le \eta_y < 0.50$: `-` or `░`
- $0.50 \le \eta_y < 0.80$: `.`
- $\eta_y \ge 0.80$ (horizon): ` ` (empty)

---

## 7. Grid Collision Detection & Wall Sliding

To prevent the player from clipping into walls or tunneling through corners, the player is modeled with a collision buffer circle of radius $R = 0.20$ map units.

### Intersection Check
A position $(p_x, p_y)$ is in collision if any of the following 5 sample points falls within a solid wall or outside the map:
$$(p_x, p_y), \quad (p_x \pm R, p_y \pm R)$$

### Wall Sliding Algorithm
When moving by $(\Delta x, \Delta y)$:
1. **Try Full Move:** Check collision at $(p_x + \Delta x, p_y + \Delta y)$. If clear, update position:
   $$p_x \leftarrow p_x + \Delta x, \quad p_y \leftarrow p_y + \Delta y$$
2. **Slide on X:** If blocked, test $(p_x + \Delta x, p_y)$. If clear:
   $$p_x \leftarrow p_x + \Delta x$$
3. **Slide on Y:** If still blocked, test $(p_x, p_y + \Delta y)$. If clear:
   $$p_y \leftarrow p_y + \Delta y$$
4. **Halt:** If both axes are blocked, movement is arrested and collision feedback is reported.
