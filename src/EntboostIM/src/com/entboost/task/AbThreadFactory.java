/*
 * Copyright (C) 2015 www.amsoft.cn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.entboost.task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Process;

import com.entboost.utils.AbAppUtil;

public class AbThreadFactory {
	
	/** 任务执行器. */
	public static Executor mExecutorService = null;
	
	/** 保存线程数量 . */
	private static final int CORE_POOL_SIZE = 5;
	
	/** 最大线程数量 . */
    private static final int MAXIMUM_POOL_SIZE = 64;
    
    /** 活动线程数量 . */
    private static final int KEEP_ALIVE = 5;

    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AbThread #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> mPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(10);
    
    /**
	 * 获取执行器.
	 */
    public static Executor getExecutorService() { 
        if (mExecutorService == null) { 
        	int numCores = AbAppUtil.getNumCores();
        	mExecutorService
	         = new ThreadPoolExecutor(numCores * CORE_POOL_SIZE,numCores * MAXIMUM_POOL_SIZE,numCores * KEEP_ALIVE,
                    TimeUnit.SECONDS, mPoolWorkQueue, mThreadFactory);
        }
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        return mExecutorService;
    } 
	
	
}
