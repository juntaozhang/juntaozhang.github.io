package org.example.java.pattern.behavioral.state;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态的转换基本上都是内部行为，主要在状态模式内部来维护。
 * 比如对于投票的人员，任何时候他的操作都是投票，但是投票管理对象的处理却不一定一样，
 * 会根据投票的次数来判断状态，然后根据状态去选择不同的处理。
 */
interface VoteState {
  /**
   * 处理状态对应的行为
   *
   * @param user        投票人
   * @param voteItem    投票项
   * @param voteManager 投票上下文，用来在实现状态对应的功能处理的时候，
   *                    可以回调上下文的数据
   */
  public void vote(String user, String voteItem, VoteManager voteManager);
}

class NormalVoteState implements VoteState {

  @Override
  public void vote(String user, String voteItem, VoteManager voteManager) {
    //正常投票，记录到投票记录中
    voteManager.getMapVote().put(user, voteItem);
    System.out.println("恭喜投票成功");
  }

}

class RepeatVoteState implements VoteState {

  @Override
  public void vote(String user, String voteItem, VoteManager voteManager) {
    //重复投票，暂时不做处理
    System.out.println("请不要重复投票");
  }

}

class SpiteVoteState implements VoteState {

  @Override
  public void vote(String user, String voteItem, VoteManager voteManager) {
    // 恶意投票，取消用户的投票资格，并取消投票记录
    String str = voteManager.getMapVote().get(user);
    if (str != null) {
      voteManager.getMapVote().remove(user);
    }
    System.out.println("你有恶意刷屏行为，取消投票资格");
  }

}

class BlackVoteState implements VoteState {

  @Override
  public void vote(String user, String voteItem, VoteManager voteManager) {
    //记录黑名单中，禁止登录系统
    System.out.println("进入黑名单，将禁止登录和使用本系统");
  }

}

class VoteManager {
  //持有状体处理对象
  private VoteState state = null;
  //记录用户投票的结果，Map<String,String>对应Map<用户名称，投票的选项>
  private Map<String, String> mapVote = new HashMap<String, String>();
  //记录用户投票次数，Map<String,Integer>对应Map<用户名称，投票的次数>
  private Map<String, Integer> mapVoteCount = new HashMap<String, Integer>();

  /**
   * 获取用户投票结果的Map
   */
  public Map<String, String> getMapVote() {
    return mapVote;
  }

  /**
   * 投票
   *
   * @param user     投票人
   * @param voteItem 投票的选项
   */
  public void vote(String user, String voteItem) {
    //1.为该用户增加投票次数
    //从记录中取出该用户已有的投票次数
    Integer oldVoteCount = mapVoteCount.get(user);
    if (oldVoteCount == null) {
      oldVoteCount = 0;
    }
    oldVoteCount += 1;
    mapVoteCount.put(user, oldVoteCount);
    //2.判断该用户的投票类型，就相当于判断对应的状态
    //到底是正常投票、重复投票、恶意投票还是上黑名单的状态
    if (oldVoteCount == 1) {
      state = new NormalVoteState();
    } else if (oldVoteCount > 1 && oldVoteCount < 5) {
      state = new RepeatVoteState();
    } else if (oldVoteCount >= 5 && oldVoteCount < 8) {
      state = new SpiteVoteState();
    } else if (oldVoteCount > 8) {
      state = new BlackVoteState();
    }
    //然后转调状态对象来进行相应的操作
    state.vote(user, voteItem, this);
  }
}

public class Client {
  public static void main(String[] args) {

    VoteManager vm = new VoteManager();
    for (int i = 0; i < 9; i++) {
      vm.vote("hans", "A");
    }
  }
}
