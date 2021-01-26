import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

class Pool {
    //任务仓库
    private BlockingQueue<Runnable> taskQueue;

    //工作线程
    private List<Worker> workerList;

    //线程可见 防止指令重排
    private volatile boolean working = true;

    public Pool (int poolSize, int taskQueueSize) {
        taskQueue = new LinkedBlockingDeque<>(taskQueueSize);
        workerList = new ArrayList<>(poolSize);

        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(this);
            workerList.add(worker);
            worker.start();
        }
    }

    public void shutdown() {
        //不能接受新任务，原有任务执行完成后
        this.working = false;

        for (Thread thread : this.workerList) {
            if (thread.getState().equals(Thread.State.BLOCKED)
                    || thread.getState().equals(Thread.State.WAITING)
                    || thread.getState().equals(Thread.State.TIMED_WAITING)) {

                //中断线程的阻塞等待状态
                thread.interrupt();
            }
        }

    }

    public boolean submit(Runnable runnable) {
        if(working) {
            return taskQueue.offer(runnable);
        }
        return false;
    }

    private static class Worker extends Thread{
        private Pool pool;
        public Worker(Pool pool) {
            this.pool = pool;
        }
        
        public void run() {
            //不断从任务仓库那任务执行
            while(this.pool.working || this.pool.taskQueue.size() > 0) {
                Runnable task  = null;
                try {
                    if(this.pool.working){
                        task = pool.taskQueue.take();
                    } else {
                        task = pool.taskQueue.poll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(task != null) {
                    try{
                        //执行任务
                         task.run();

                    } catch (Exception e){
                        //处理异常
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        Pool pool = new Pool(3, 6);
        for (int i = 0; i < 5; i++) {
            final int index = i;
            pool.submit(() -> {
                try {
                    Thread.sleep(200l);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(Thread.currentThread().getName() + "执行 任务" + index + "开始执行。。。。");
            });

        }
        try {
            Thread.sleep(300l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pool.shutdown();
    }

}
