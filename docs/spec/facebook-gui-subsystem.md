# Book of Many Faces GUI 子系统规格 (Subsystem Spec)

- 里程碑 / Stage：M6 / Stage H2-GUI
- 关联平行任务：GUI-COMPLETION（本框架）；读 facebook-subsystem.md（变脸逻辑）
- 状态：完整实现，包含分页显示、晶体尺寸图标、滚动浏览
- 负责：GUI-COMPLETION
- 最后更新：2026-07-27

## 1. 概述 / 目标

Book of Many Faces 的容器GUI界面，显示玩家收集的所有变脸条目（名称 + 晶体尺寸），支持分页浏览。这是一个记录本式GUI，不直接执行变脸操作。

## 2. 范围

- 含：`client/screen/item/BookOfManyFacesScreen` - 分页GUI与条目渲染
- 含：`textures/gui/face_book.png` - 256x256 特殊书本纹理
- 含：`content/menu/facebook/BookOfManyFacesMenu.FaceEntry` - 条目数据结构
- 不含：变脸逻辑（已在 facebook-subsystem.md）
- 不含：lang翻译键（PK-4）

## 3. 设计 / 架构

### 关键类
- `BookOfManyFacesScreen extends AbstractContainerScreen<BookOfManyFacesMenu>`
  - `imageWidth = 176, imageHeight = 160`：紧凑书本尺寸（比标准容器矮）
  - `page`：当前页码（0-based）
  - `previous` / `next`：翻页按钮（20x18）
  - `turn(direction)`：翻页逻辑 + 按钮可见性更新
  - `isPauseScreen() = false`：不暂停游戏

### 布局设计
```
标题："Book of Many Faces" (20, 16)
列标题："Crystal Size" (右对齐，imageWidth-22-width, 16)
条目列表（每条20像素间隔）:
  - 条目1名称 (20, 28) + 晶体图标 (115, 26)
  - 条目2名称 (20, 48) + 晶体图标 (115, 46)
  - 条目3名称 (20, 68) + 晶体图标 (115, 66)
  - 条目4名称 (20, 88) + 晶体图标 (115, 86)
  - 条目5名称 (20, 108) + 晶体图标 (115, 106)
翻页按钮:
  - previous: (20, 134, 20x18) - 当page>0时visible
  - next: (132, 134, 20x18) - 当page+1<pageCount时visible
```

### 晶体尺寸映射
```java
crystalForSize(int size):
  case 1 → MaterialItems.CRYSTALS.get(0) // 小晶体
  case 2 → CrystalClusterBlocks.CLUSTERS.get(0) // 晶簇
  default → MaterialItems.CRYSTAL_SHARDS.get(0) // 碎片
```

### Menu数据契约
- `menu.pageCount()` - 总页数
- `menu.page(int)` - 获取指定页的条目列表（最多5条）
- `FaceEntry.name()` - 变脸名称（可能很长，需要换行）
- `FaceEntry.crystalSize()` - 晶体尺寸（0/1/2）

## 4. 子系统内契约

- 对外API：`BookOfManyFacesScreen` 由 `MenuScreens.register` 注册到 `BookOfManyFacesMenu`
- 读 Menu：只读分页数据，无写操作
- 文字换行：使用 `font.split(Component.literal(name), 90)` 限制90像素宽度
- 双行显示：如果名称过长，第一行y，第二行y+9
- 图标渲染：使用 `GuiGraphics.renderItem` 在固定x=115位置

## 5. 跨版本 / 加载器要点

- 兼容层：`ClientScreenCompat.background` - 1.20.1/1.21 renderBackground签名差异
- `//?` 分叉点：无业务分叉
- 纹理格式：256x256 PNG（书本纹理，非标准容器背景）
- 按钮标签：使用 `Component.literal("<")` / `Component.literal(">")` 简单箭头
- 不暂停游戏：覆写 `isPauseScreen() = false`（记录本类GUI常见行为）

## 6. 实现记忆 / 踩坑

- **imageHeight=160而非166**：书本GUI比标准容器矮6像素
- **按钮y=134**：接近底部但留空间，与160高度匹配
- **双行文字布局**：第二行y+9而非y+10（字体lineHeight=9）
- **updateButtons()必调**：init和turn都需要更新按钮可见性
- **page边界检查**：turn时使用 `Math.max(0, Math.min(pageCount-1, page+direction))`
- **晶体图标y偏移**：y-2使图标与文字基线对齐
- **名称过长处理**：最多两行，超出部分裁剪（不显示省略号）

## 7. 验证 / DoD

- ✅ PNG格式验证：256x256，真PNG文件头
- ✅ blit尺寸匹配：imageWidth=176, imageHeight=160，特殊书本尺寸
- ✅ 双端编译：无错误，AbstractContainerScreen继承正确
- ✅ 分页逻辑：按钮可见性随页码更新
- ✅ 文字换行：90像素宽度限制，最多两行
- ✅ 晶体图标：三种尺寸正确映射到物品图标
- ✅ 不暂停游戏：覆写isPauseScreen返回false
- 未机核：真人打开书本翻页观察5+条目分页显示与长名称换行

## 修订日志

- 2026-07-27：GUI-COMPLETION 创建完整spec，验证PNG、分页逻辑、文字换行与双端编译
