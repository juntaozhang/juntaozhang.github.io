package org.example.java.pattern.creational.factory;

interface Cpu {
  public void calculate();
}

interface MainBoard {
  public void installCPU();
}

class IntelCpu implements Cpu {
  /**
   * CPU的针脚数
   */
  private int pins = 0;

  public IntelCpu(int pins) {
    this.pins = pins;
  }

  @Override
  public void calculate() {
    System.out.println("Intel CPU的针脚数：" + pins);
  }

}

class AmdCpu implements Cpu {
  /**
   * CPU的针脚数
   */
  private int pins = 0;

  public AmdCpu(int pins) {
    this.pins = pins;
  }

  @Override
  public void calculate() {
    System.out.println("AMD CPU的针脚数：" + pins);
  }
}

class IntelMainBoard implements MainBoard {
  /**
   * CPU插槽的孔数
   */
  private int cpuHoles = 0;

  public IntelMainBoard(int cpuHoles) {
    this.cpuHoles = cpuHoles;
  }

  @Override
  public void installCPU() {
    System.out.println("Intel主板的CPU插槽孔数是：" + cpuHoles);
  }

}

class AmdMainBoard implements MainBoard {
  /**
   * CPU插槽的孔数
   */
  private int cpuHoles = 0;

  public AmdMainBoard(int cpuHoles) {
    this.cpuHoles = cpuHoles;
  }

  @Override
  public void installCPU() {
    System.out.println("AMD主板的CPU插槽孔数是：" + cpuHoles);
  }
}

interface AbstractFactory {
  public Cpu createCpu();

  public MainBoard createMainBoard();
}

class IntelFactory implements AbstractFactory {

  @Override
  public Cpu createCpu() {
    return new IntelCpu(755);
  }

  @Override
  public MainBoard createMainBoard() {
    return new IntelMainBoard(755);
  }

}

class AmdFactory implements AbstractFactory {

  @Override
  public Cpu createCpu() {
    return new AmdCpu(938);
  }

  @Override
  public MainBoard createMainBoard() {
    return new AmdMainBoard(938);
  }

}

class ComputerEngineer {

  public void makeComputer(AbstractFactory af) {
    /**
     * 组装机器的基本步骤
     */
    //1:首先准备好装机所需要的配件
    prepareHardware(af);
    //2:组装机器
    //3:测试机器
    //4：交付客户
  }

  private void prepareHardware(AbstractFactory af) {
    //直接找相应的工厂获取
    Cpu cpu = af.createCpu();
    MainBoard mainboard = af.createMainBoard();

    //测试配件是否好用
    cpu.calculate();
    mainboard.installCPU();
  }
}

public class Client {
  public static void main(String[] args) {
    //创建装机工程师对象
    ComputerEngineer cf = new ComputerEngineer();
    //客户选择并创建需要使用的产品对象
    AbstractFactory af = new IntelFactory();
    //告诉装机工程师自己选择的产品，让装机工程师组装电脑
    cf.makeComputer(af);

    af = new AmdFactory();
    //告诉装机工程师自己选择的产品，让装机工程师组装电脑
    cf.makeComputer(af);
  }
}
