/*
BSD 3-Clause License

Copyright (c) 2007-2013, Distributed Computing Group (DCG)
                         ETH Zurich
                         Switzerland
                         dcg.ethz.ch
              2017-2018, André Brait

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.blockchain;

import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.nodeCollection.AbstractNodeCollection;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.swing.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import projects.blockchain.csv.PathEnum;
import projects.blockchain.csv.models.ChainSizeEntry;
import projects.blockchain.nodes.nodeImplementations.AmbientalBlockchainNode;

/**
 * This class holds customized global state and methods for the framework. The
 * only mandatory method to overwrite is <code>hasTerminated</code> <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 *
 * @see sinalgo.runtime.AbstractCustomGlobal for more details. <br>
 * In addition, this class also provides the possibility to extend the
 * framework with custom methods that can be called either through the menu
 * or via a button that is added to the GUI.
 */
public class CustomGlobal extends AbstractCustomGlobal {


    Logging logging = Logging.getLogger();

    ArrayList<ChainSizeEntry> chainSizeHistory = new ArrayList<ChainSizeEntry>();

    @Override
    public boolean hasTerminated() {
        return false;
    }

    /**
     * An example of a method that will be available through the menu of the GUI.
     */
    @AbstractCustomGlobal.GlobalMethod(menuText = "Echo")
    public void echo() {
        // Query the user for an input
        String answer = JOptionPane.showInputDialog(null, "This is an example.\nType in any text to echo.");
        // Show an information message
        JOptionPane.showMessageDialog(null, "You typed '" + answer + "'", "Example Echo",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * An example to add a button to the user interface. In this sample, the button
     * is labeled with a text 'GO'. Alternatively, you can specify an icon that is
     * shown on the button. See AbstractCustomGlobal.CustomButton for more details.
     */
    @AbstractCustomGlobal.CustomButton(buttonText = "GO", toolTipText = "A sample button")
    public void sampleButton() {
        JOptionPane.showMessageDialog(null, "You Pressed the 'GO' button.");
    }

    @Override
    public void preRun()
    {
      this.logging.logln("******* Started Application ******");
      try{
        this.createCSVFile(PathEnum.TOTAL_ORPHANS.getPath());
        this.createCSVFile(PathEnum.TOTAL_REPLACEMENTS.getPath());
        this.createCSVFile(PathEnum.TOTAL_MESSAGES.getPath());
      } catch (Exception e)
      {
        this.logging.logln(e.toString());
      }
    }

    @Override
    public void preRound()
    {
    }
    @Override
    public void postRound()
    {
      AbstractNodeCollection nodes = Tools.getNodeList();
      for (Node n : nodes)
      {
        AmbientalBlockchainNode node = (AmbientalBlockchainNode) n;
        this.chainSizeHistory.add(new ChainSizeEntry(node.getID(), (int) Tools.getGlobalTime(), node.getChain().size()));
      }
    }

    @Override
    public void onExit()
    {
      try
      {
        LinkedHashMap<Long, Integer> orphansMap = new LinkedHashMap<Long, Integer>();
        LinkedHashMap<Long, Integer> replacementsMap = new LinkedHashMap<Long, Integer>();
        LinkedHashMap<Long, Integer> messagesMap = new LinkedHashMap<Long, Integer>();
        
        AbstractNodeCollection nodes = Tools.getNodeList();
        for (Node n : nodes)
        {
          AmbientalBlockchainNode node = (AmbientalBlockchainNode) n;
          this.logging.logln(String.format("**** Node %1$s: total orphans: %2$s *****", node.getID(), node.getOrphans().size()));
          orphansMap.put( node.getID(), node.getOrphans().size());
          this.logging.logln(String.format("**** Node %1$s: total replacements: %2$s *****", node.getID(), node.getTotalChainReplacements()));
          replacementsMap.put( node.getID(), node.getTotalChainReplacements());
          this.logging.logln(String.format("**** Node %1$s: total messages: %2$s *****", node.getID(), node.getTotalMessages()));
          messagesMap.put( node.getID(), node.getTotalMessages());

          ArrayList<ChainSizeEntry> l = (ArrayList<ChainSizeEntry>) this.chainSizeHistory
            .stream()
            .filter((ChainSizeEntry entry) -> {
              return entry.getID() == node.getID();
            })
            .collect(Collectors.toList());

            this.createChainSizeCSVFile(String.format("%1$s_%2$s.csv", PathEnum.HISTORY_NODE_CHAIN_SIZE.getPath(), node.getID()), l);
        }
        
        this.createTotalsCSVFile(PathEnum.TOTAL_ORPHANS.getPath(), orphansMap, "orphans");
        this.createTotalsCSVFile(PathEnum.TOTAL_REPLACEMENTS.getPath(), replacementsMap, "replacements");
        this.createTotalsCSVFile(PathEnum.TOTAL_MESSAGES.getPath(), messagesMap, "messages");
        
        
        
      } catch (Exception e) {
        //TODO: handle exception
      }
      this.logging.logln("******* Exited Application ******");
    }

    private void createCSVFile(String path) throws IOException {
      String[] HEADERS = { "round", "Node 1 messages"};

      FileWriter out = new FileWriter(path);
      try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
        .withHeader(HEADERS))) {

      }
    }
    
    private void createTotalsCSVFile(String path, LinkedHashMap<Long, Integer> map) throws IOException {
      String[] HEADERS = { "Node ID", "KPI"};

      FileWriter out = new FileWriter(path);
      try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
        .withHeader(HEADERS))) {
          for(Entry<Long, Integer> e : map.entrySet())
          {
            printer.printRecord(e.getKey(), e.getValue());
          }
      }
    }
    private void createTotalsCSVFile(String path, LinkedHashMap<Long, Integer> map, String kpiName) throws IOException {
      String[] HEADERS = { "Node ID", kpiName};

      FileWriter out = new FileWriter(path);
      try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
        .withHeader(HEADERS))) {
          for(Entry<Long, Integer> e : map.entrySet())
          {
            printer.printRecord(e.getKey(), e.getValue());
          }
      }
    }

    private void createChainSizeCSVFile(String path, ArrayList<ChainSizeEntry> entries) throws IOException {
      String[] HEADERS = { "Node ID", "round", "size"};

      FileWriter out = new FileWriter(path);
      try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
        .withHeader(HEADERS))) {
          for(ChainSizeEntry e : entries)
          {
            printer.printRecord(e.getID(), e.getRound(), e.getChainSize());
          }
      }
    }
}
