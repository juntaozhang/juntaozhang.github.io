package org.example.java.pattern.behavioral.ChainOfResponsibility;

abstract class Handler {
  /**
   * 持有下一个处理请求的对象
   */
  protected Handler successor = null;

  public Handler getSuccessor() {
    return successor;
  }

  public void setSuccessor(Handler successor) {
    this.successor = successor;
  }

  /**
   * 处理聚餐费用的申请
   *
   * @param user 申请人
   * @param fee  申请的钱数
   * @return 成功或失败的具体通知
   */
  public abstract String handleFeeRequest(String user, double fee);
}

class ProjectManager extends Handler {

  @Override
  public String handleFeeRequest(String user, double fee) {
    String str = "";
    //项目经理权限比较小，只能在500以内
    if (fee < 500) {
      //为了测试，简单点，只同意张三的请求
      if ("张三".equals(user)) {
        str = "成功：项目经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
      } else {
        //其他人一律不同意
        str = "失败：项目经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
      }
    } else {
      //超过500，继续传递给级别更高的人处理
      if (getSuccessor() != null) {
        return getSuccessor().handleFeeRequest(user, fee);
      }
    }
    return str;
  }
}

class DeptManager extends Handler {

  @Override
  public String handleFeeRequest(String user, double fee) {
    String str = "";
    //部门经理的权限只能在1000以内
    if (fee < 1000) {
      //为了测试，简单点，只同意张三的请求
      if ("张三".equals(user)) {
        str = "成功：部门经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
      } else {
        //其他人一律不同意
        str = "失败：部门经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
      }
    } else {
      //超过1000，继续传递给级别更高的人处理
      if (getSuccessor() != null) {
        return getSuccessor().handleFeeRequest(user, fee);
      }
    }
    return str;
  }
}

class GeneralManager extends Handler {
  @Override
  public String handleFeeRequest(String user, double fee) {
    String str = "";
    //总经理的权限很大，只要请求到了这里，他都可以处理
    if (fee >= 1000) {
      //为了测试，简单点，只同意张三的请求
      if ("张三".equals(user)) {
        str = "成功：总经理同意【" + user + "】的聚餐费用，金额为" + fee + "元";
      } else {
        //其他人一律不同意
        str = "失败：总经理不同意【" + user + "】的聚餐费用，金额为" + fee + "元";
      }
    } else {
      //如果还有后继的处理对象，继续传递
      if (getSuccessor() != null) {
        return getSuccessor().handleFeeRequest(user, fee);
      }
    }
    return str;
  }
}

public class Client {
  public static void main(String[] args) {
    //先要组装责任链
    //总经理
    Handler generalManager = new GeneralManager();
    //部门主管
    Handler deptManager = new DeptManager();
    //项目经理
    Handler projectManager = new ProjectManager();

    projectManager.setSuccessor(deptManager);
    deptManager.setSuccessor(generalManager);

    //开始测试
    String test1 = projectManager.handleFeeRequest("张三", 300);
    System.out.println("test1 = " + test1);
    String test2 = projectManager.handleFeeRequest("李四", 300);
    System.out.println("test2 = " + test2);
    System.out.println("---------------------------------------");

    String test3 = projectManager.handleFeeRequest("张三", 700);
    System.out.println("test3 = " + test3);
    String test4 = projectManager.handleFeeRequest("李四", 700);
    System.out.println("test4 = " + test4);
    System.out.println("---------------------------------------");

    String test5 = projectManager.handleFeeRequest("张三", 1500);
    System.out.println("test5 = " + test5);
    String test6 = projectManager.handleFeeRequest("李四", 1500);
    System.out.println("test6 = " + test6);
  }
}
