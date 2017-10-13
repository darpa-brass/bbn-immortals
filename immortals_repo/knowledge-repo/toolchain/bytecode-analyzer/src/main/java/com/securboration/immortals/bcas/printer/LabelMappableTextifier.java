package com.securboration.immortals.bcas.printer;


import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Textifier;

public class LabelMappableTextifier extends Textifier
{
  public LabelMappableTextifier()
  {
    super(Opcodes.ASM5);
  }
  
  public void registerLabel(Label label)
  {
    Map<Label,String> labels = getLabelMapping();
    
    if(labels.containsKey(label))
    {
      return;
    }
    
    labels.put(label, String.format("L%d",labels.size()));
  }
  
  private Map<Label,String> getLabelMapping()
  {
    if(this.labelNames == null)
    {
      this.labelNames = new HashMap<>();
    }
    
    return this.labelNames;
  }
}
