package myplugin;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

public class ExportActiveDiagramGeometryAction extends MDAction {

    private static final String ACTION_ID = "DEMO_EXPORT_ACTIVE_DIAGRAM_GEOMETRY";

    public ExportActiveDiagramGeometryAction() {
        super(ACTION_ID, "Export Active Diagram to JSON", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog log = Application.getInstance().getGUILog();
        Project project = Application.getInstance().getProject();

        if (project == null) {
            log.log("[ERROR] No project opened.");
            return;
        }
        DiagramPresentationElement diagram = project.getActiveDiagram();
        if (diagram == null) {
            log.log("[ERROR] No active diagram. Please open an Activity Diagram tab first.");
            return;
        }

        File outFile = chooseOutputFile(diagram.getName(), log);
        if (outFile == null) return;

        try {
            String json = exportDiagramToJson(diagram);
            Files.write(outFile.toPath(), json.getBytes(StandardCharsets.UTF_8));
            log.log("[INFO] Exported geometry JSON to: " + outFile.getAbsolutePath());
        } catch (Exception ex) {
            log.log("[ERROR] Export failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private File chooseOutputFile(String diagramName, GUILog log) {
        String safeName = diagramName == null ? "diagram" : diagramName.replaceAll("[\\\\/:*?\"<>|]", "_");

        File outDir = new File("D:/Projects/IDEA/untitled1");
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        File f = new File(outDir, safeName + ".json");
        log.log("[INFO] Auto export path: " + f.getAbsolutePath());
        return f;
    }

    private String exportDiagramToJson(DiagramPresentationElement diagram) {
        List<PresentationElement> pes = collectAllPresentationElements(diagram);
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        GUILog log = Application.getInstance().getGUILog();
        for (PresentationElement pe : pes) {
            Element modelElement = pe.getElement();
            String pv = pe.getClass().getSimpleName();

            if (modelElement == null && !"NoteView".equals(pv)) {
                continue;
            }

            if (pv.contains("Text") || pv.contains("Label") || pv.contains("Header")
                    || pv.contains("Compartment") || pv.contains("Stereotype") || "DiagramFrameView".equals(pv)) {
                continue;
            }

            String elementId = (modelElement != null) ? safe(modelElement.getID()) : null;
            String elementType = (modelElement != null) ? modelElement.getClass().getSimpleName() : pe.getClass().getSimpleName();

            if (pe instanceof ShapeElement) {
                ShapeElement se = (ShapeElement) pe;
                Rectangle r = se.getBounds();
                Map<String, Object> n = new LinkedHashMap<>();
                n.put("id", elementId);
                n.put("type", elementType);
                n.put("presentationType", se.getClass().getSimpleName());

                String nodeName = "";
                if (modelElement instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) {
                    com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement ne =
                            (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement) modelElement;
                    nodeName = safe(ne.getName());
                }
                n.put("name", nodeName);
                n.put("x", r.x);
                n.put("y", r.y);
                n.put("w", r.width);
                n.put("h", r.height);

                // ── 新增：Pin 节点单独导出 label 的真实坐标 ──
                if ("OutputPinImpl".equals(elementType) || "InputPinImpl".equals(elementType)) {
                    Collection<PresentationElement> children = se.getPresentationElements();
                    if (children != null) {
                        for (PresentationElement child : children) {
                            // MagicDraw 里 Pin 的文字标签是 TextPresentationElement 子元素
                            if (child.getClass().getSimpleName().contains("Text")
                                    || child.getClass().getSimpleName().contains("Label")) {
                                Rectangle lb = child.getBounds();
                                if (lb != null && lb.width > 0) {
                                    Map<String, Object> labelBounds = new LinkedHashMap<>();
                                    labelBounds.put("x", lb.x);
                                    labelBounds.put("y", lb.y);
                                    labelBounds.put("w", lb.width);
                                    labelBounds.put("h", lb.height);
                                    n.put("labelBounds", labelBounds);
                                    break;
                                }
                            }
                        }
                    }
                }
                // 在 exportDiagramToJson 的 ShapeElement 处理块里，临时加到 nodes.add(n) 之前
                if ("OutputPinImpl".equals(elementType) || "InputPinImpl".equals(elementType)) {
                    Collection<PresentationElement> children = se.getPresentationElements();
                    if (children != null) {
                        for (PresentationElement child : children) {
                            // 打印每个子元素的类型，看看到底叫什么
                            log.log("[DEBUG] Pin child type: " + child.getClass().getName()
                                    + " | simpleName: " + child.getClass().getSimpleName()
                                    + " | bounds: " + child.getBounds());
                        }
                    }
                }
                nodes.add(n);
            } else if (pe instanceof PathElement) {
                PathElement path = (PathElement) pe;
                List<Point> pts = path.getAllBreakPoints();

                Map<String, Object> ed = new LinkedHashMap<>();
                ed.put("id", elementId);
                ed.put("type", elementType);
                ed.put("presentationType", path.getClass().getSimpleName());
                ed.put("points", pointsToList(pts));

                try {
                    if (modelElement instanceof com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge) {
                        com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge ae =
                                (com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge) modelElement;
                        com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode src = ae.getSource();
                        com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode dst = ae.getTarget();
                        if (src != null) ed.put("sourceId", src.getID());
                        if (dst != null) ed.put("targetId", dst.getID());
                    }
                } catch (Throwable ignore) {}

                edges.add(ed);
            }
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("diagram", new LinkedHashMap<String, Object>() {{
            put("name", safe(diagram.getName()));
            put("diagramType", String.valueOf(diagram.getDiagramType()));
        }});
        root.put("nodes", nodes);
        root.put("edges", edges);

        return toJson(root);
    }

    private List<PresentationElement> collectAllPresentationElements(PresentationElement root) {
        List<PresentationElement> out = new ArrayList<>();
        Deque<PresentationElement> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            PresentationElement cur = stack.pop();
            if (cur == null) continue;
            out.add(cur);
            Collection<PresentationElement> children = cur.getPresentationElements();
            if (children != null) {
                for (PresentationElement ch : children) {
                    if (ch != null) stack.push(ch);
                }
            }
        }
        return out;
    }

    //把 Java 的 Point 对象转换成 JSON 能表示的二维数组
    private List<List<Integer>> pointsToList(List<Point> pts) {
        List<List<Integer>> out = new ArrayList<>();
        if (pts == null) return out;
        for (Point p : pts) {
            if (p == null) continue;
            out.add(Arrays.asList(p.x, p.y));
        }
        return out;
    }

    private static final String INDENT = "  ";

    //把 Java 对象 Map / List / String / Number / Boolean 转成 JSON 字符串
    private String toJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        writeJson(sb, obj, 0);
        sb.append("\n");
        return sb.toString();
    }
    //JSON 的缩进格式
    private void indent(StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) sb.append(INDENT);
    }

    @SuppressWarnings("unchecked")
    private void writeJson(StringBuilder sb, Object obj, int level) {
        if (obj == null) {
            sb.append("null");
            return;
        }
        if (obj instanceof String) {
            sb.append("\"").append(escape((String) obj)).append("\"");
            return;
        }
        if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj.toString());
            return;
        }
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                indent(sb, level + 1);
                sb.append("\"").append(escape(e.getKey())).append("\": ");
                writeJson(sb, e.getValue(), level + 1);
                if (++i < map.size()) sb.append(",");
                sb.append("\n");
            }
            indent(sb, level);
            sb.append("}");
            return;
        }
        if (obj instanceof Collection) {
            Collection<?> list = (Collection<?>) obj;
            if (list.isEmpty()) {
                sb.append("[]");
                return;
            }
            sb.append("[\n");
            int i = 0;
            int n = list.size();
            for (Object it : list) {
                indent(sb, level + 1);
                writeJson(sb, it, level + 1);
                if (++i < n) sb.append(",");
                sb.append("\n");
            }
            indent(sb, level);
            sb.append("]");
            return;
        }
        sb.append("\"").append(escape(obj.toString())).append("\"");
    }
    //转义 JSON 字符串中的特殊字符
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    //防止空指针
    private String safe(String s) {
        return s == null ? "" : s;
    }
}