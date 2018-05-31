package com.ef.config;

import com.ef.batch.job.ParserJob;
import com.ef.batch.listener.JobCompletionNotificationListener;
import com.ef.batch.step.BlockUserIpStep;
import com.ef.batch.step.EmptyUserLogTableStep;
import com.ef.batch.step.FileLoaderStep;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Here we register all of our spring configuration classes. It also contains a Spring context object.
 *
 * @author yinfante
 */
public class SpringRegistry {

    // spring context
    private static AnnotationConfigApplicationContext context;

    /**
     * Registering all our spring configuration classes.
     */
    static {
        context = new AnnotationConfigApplicationContext();

        context.register(EmptyUserLogTableStep.class);
        context.register(ParserJob.class);
        context.register(Configurations.class);
        context.register(JobCompletionNotificationListener.class);
        context.register(BlockUserIpStep.class);
        context.register(FileLoaderStep.class);

        refreshContext();
    }

    /**
     * It returns an instance of spring context
     *
     * @return an AnnotationConfigApplicationContext instance
     * @see AnnotationConfigApplicationContext
     */
    public static AnnotationConfigApplicationContext getContext() {
        return context;
    }

    /**
     * It refreshes spring context
     */
    public static void refreshContext() {
        context.refresh();
    }


}
