package com.securboration.immortals.bcad.callgraph;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import com.securboration.immortals.bcad.callgraph.GraphBuildingListener.StackStateVisitor;

public class CallgraphBuilder implements StackStateVisitor {

    private final Map<Thread, Map<Edge, AtomicLong>> threadsToCounts =
        new HashMap<>();

    private void visitEdge(String from, String to) {
        Thread t = Thread.currentThread();

        Map<Edge, AtomicLong> edgeCounts = threadsToCounts.get(t);

        if (edgeCounts == null) {
            edgeCounts = new HashMap<>();
            threadsToCounts.put(t, edgeCounts);
        }

        Edge edge = new Edge(from, to);

        AtomicLong count = edgeCounts.get(edge);

        if (count == null) {
            count = new AtomicLong(0l);
            edgeCounts.put(edge, count);
        }

        count.incrementAndGet();
    }

    private static String getStackEntry(Stack<String> stack, int offset) {
        return stack.get(stack.size() - 1 + offset);
    }

    @Override
    public void postEntry(Stack<String> currentCallStack) {
        if (currentCallStack.size() > 1) {
            final String from = getStackEntry(currentCallStack, -1);
            final String to = getStackEntry(currentCallStack, 0);

            visitEdge(from, to);
        }
    }

//    public void dump(Map<Edge, AtomicLong> counts) {
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("edges:\n");
//        for (Edge e : counts.keySet()) {
//            sb.append(e.getFrom());
//            sb.append(" ");
//            sb.append(e.getTo());
//            sb.append(" ");
//            sb.append(counts.get(e).get());
//            sb.append("\n");
//        }
//
//        sb.append("\ndictionary:\n");
//        Set<String> entries = new HashSet<>();
//        for (Edge e : counts.keySet()) {
//            entries.add(e.getFrom());
//            entries.add(e.getTo());
//        }
//        for (String entry : entries) {
//            sb.append(entry);
//            sb.append("\n");
//        }
//
//    }

    @Override
    public void postReturn(Stack<String> currentCallStack) {
    }

    @Override
    public void postCatch(Stack<String> currentCallStack, Throwable t) {
    }

    @Override
    public void postUncaught(Stack<String> currentCallStack, Throwable t) {
    }

    @Override
    public void postControlFlow(Stack<String> currentCallStack) {
    }

    
    public Map<Thread, Map<Edge, AtomicLong>> getThreadsToCounts() {
        return threadsToCounts;
    }

}
