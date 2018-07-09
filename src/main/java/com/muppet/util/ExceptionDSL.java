package com.muppet.util;

import com.muppet.rabbitfriend.core.RabbitFriendException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by yuhaiqiang on 2018/7/8.
 *
 * @description
 */

public class ExceptionDSL {
    private static final Logger logger = LogManager.getLogger(ExceptionDSL.class);

    public static class ExceptionWrapper {
        public ExceptionWrapper throwableSafe(RunnableWithThrowable runnable) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error("unhandled throwable happened", t);
            }

            return this;
        }

        public ExceptionWrapper throwable(RunnableWithThrowable runnable, String error) {
            try {
                runnable.run();
            } catch (Throwable t) {
                throw new RabbitFriendException(error, t);
            }
            return this;
        }

        public ExceptionWrapper throwable(RunnableWithThrowable runnable) {
            try {
                runnable.run();
            } catch (Throwable t) {
                throw new RabbitFriendException(t);
            }
            return this;
        }

        public ExceptionWrapper throwableSafe(RunnableWithThrowable runnable, String msg) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error(String.format("%s, unhandled throwable happened", msg), t);
            }

            return this;
        }

        public ExceptionWrapper throwableSafeSuppress(RunnableWithThrowable runnable, Class<? extends Throwable>... tclazz) {
            try {
                runnable.run();
            } catch (Throwable t) {
                boolean suppress = false;
                for (Class<? extends Throwable> tc : tclazz) {
                    if (tc.isAssignableFrom(t.getClass())) {
                        suppress = true;
                        break;
                    }
                }

                if (!suppress) {
                    logger.error(String.format("unhandled throwable happened"), t);
                }
            }

            return this;
        }

        public ExceptionWrapper throwableSafe(Runnable runnable) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error("unhandled throwable happened", t);
            }

            return this;
        }

        public ExceptionWrapper throwableSafe(Runnable runnable, String msg) {
            try {
                runnable.run();
            } catch (Throwable t) {
                logger.error(String.format("%s, unhandled throwable happened", msg), t);
            }

            return this;
        }

        public ExceptionWrapper throwableSafeSuppress(Runnable runnable, Class<? extends Throwable>... tclazz) {
            try {
                runnable.run();
            } catch (Throwable t) {
                boolean suppress = false;
                for (Class<? extends Throwable> tc : tclazz) {
                    if (tc.isAssignableFrom(t.getClass())) {
                        suppress = true;
                        break;
                    }
                }

                if (!suppress) {
                    logger.error(String.format("unhandled throwable happened"), t);
                }
            }

            return this;
        }

        public ExceptionWrapper exceptionSafe(Runnable runnable) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("unhandled throwable happened", e);
            }
            return this;
        }

        public ExceptionWrapper exceptionSafe(Runnable runnable, String msg) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error(String.format("%s, unhandled throwable happened", msg), e);
            }
            return this;
        }

        public ExceptionWrapper exceptionSafeSuppress(Runnable runnable, Class<? extends Exception>... eclazz) {
            try {
                runnable.run();
            } catch (Exception e) {
                boolean suppress = false;
                for (Class<? extends Exception> ec : eclazz) {
                    if (ec.isAssignableFrom(e.getClass())) {
                        suppress = true;
                        break;
                    }
                }

                if (!suppress) {
                    logger.error(String.format("unhandled throwable happened"), e);
                }
            }
            return this;
        }
    }

    private static ExceptionWrapper self = new ExceptionWrapper();

    public static interface RunnableWithThrowable {
        void run() throws Throwable;
    }

    public static ExceptionWrapper throwable(RunnableWithThrowable runnable) {
        return self.throwable(runnable);
    }

    public static ExceptionWrapper throwable(String msg, RunnableWithThrowable runnable) {
        return self.throwable(runnable, msg);
    }

    public static ExceptionWrapper throwableSafeSuppress(RunnableWithThrowable runnable, Class<? extends Throwable>... tclazz) {
        return self.throwableSafeSuppress(runnable, tclazz);
    }

    public static ExceptionWrapper throwableSafe(Runnable runnable) {
        return self.throwableSafe(runnable);
    }

    public static ExceptionWrapper throwableSafe(Runnable runnable, String msg) {
        return self.throwableSafe(runnable, msg);
    }

    public static ExceptionWrapper throwableSafeSuppress(Runnable runnable, Class<? extends Throwable>... tclazz) {
        return self.throwableSafeSuppress(runnable, tclazz);
    }

    public static ExceptionWrapper exceptionSafe(Runnable runnable) {
        return self.exceptionSafe(runnable);
    }

    public static ExceptionWrapper exceptionSafe(Runnable runnable, String msg) {
        return self.exceptionSafe(runnable, msg);
    }

    public static ExceptionWrapper exceptionSafeSuppress(Runnable runnable, Class<? extends Exception>... eclazz) {
        return self.exceptionSafeSuppress(runnable, eclazz);
    }

    public static boolean isCausedBy(Throwable t, Class<? extends Throwable> causeClass) {
        if (causeClass.isAssignableFrom(t.getClass())) {
            return true;
        }

        while (t.getCause() != null) {
            t = t.getCause();
            if (causeClass.isAssignableFrom(t.getClass())) {
                return true;
            }
        }

        return false;
    }

    public static Throwable getRootThrowable(Throwable t) {
        Throwable ret = t;
        while (t.getCause() != null) {
            t = ret = t.getCause();
        }
        return ret;
    }
}
