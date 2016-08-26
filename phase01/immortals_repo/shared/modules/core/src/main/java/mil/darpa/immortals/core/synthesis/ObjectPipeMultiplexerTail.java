package mil.darpa.immortals.core.synthesis;

import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This allows synchronized usage of two elements to create another
 * See {@link ObjectPipeMultiplexerHead} for an interface to synchronized the head
 * of the pipelines
 * <p>
 * Created by awellman@bbn.com on 6/21/16.
 */
public abstract class ObjectPipeMultiplexerTail<InputType0, InputType1, OutputType> {

    public final WriteableObjectPipeInterface<InputType0> input0;

    public final WriteableObjectPipeInterface<InputType1> input1;

    private InputType0 processedObject0;
    private InputType1 processedObject1;

    private final WriteableObjectPipeInterface<OutputType> next;
    private final Queue<OutputType> outputQueue;

    public ObjectPipeMultiplexerTail(WriteableObjectPipeInterface<OutputType> outputPipe) {
        if (outputPipe == null) {
            this.next = null;
            outputQueue = new ConcurrentLinkedQueue<>();
        } else {
            this.next = outputPipe;
            outputQueue = null;

        }

        final ObjectPipeMultiplexerTail multiplexer = this;

        input0 = new WriteableObjectPipeInterface<InputType0>() {
            @Override
            public void consume(InputType0 input) {
                multiplexer.writeInput0(input);
            }

            @Override
            public void flushPipe() {

            }

            @Override
            public void closePipe() {

            }
        };

        input1 = new WriteableObjectPipeInterface<InputType1>() {
            @Override
            public void consume(InputType1 input) {
                multiplexer.writeInput1(input);
            }

            @Override
            public void flushPipe() {

            }

            @Override
            public void closePipe() {

            }
        };

    }

    private synchronized void writeInput0(InputType0 input0) {
        processedObject0 = input0;
        tryExecute();
    }

    private synchronized void writeInput1(InputType1 input1) {
        processedObject1 = input1;
        tryExecute();
    }

    private synchronized void tryExecute() {
        if (processedObject0 != null && processedObject1 != null) {
            OutputType output = process(processedObject0, processedObject1);

            if (next != null) {
                next.consume(output);
            } else {
                outputQueue.add(output);
            }

            processedObject0 = null;
            processedObject1 = null;
        }
    }


    public synchronized final OutputType readNext() {
        if (outputQueue == null) {
            throw new RuntimeException("Cannot readNextObject on an ObjectPipe that connects to another!");
        }
        return outputQueue.poll();
    }

    protected abstract OutputType process(InputType0 input0, InputType1 input1);
}
