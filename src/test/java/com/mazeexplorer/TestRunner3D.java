package com.mazeexplorer;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Lightweight test runner executing all JUnit 5 test methods and reporting results.
 */
public class TestRunner3D {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   ALGORITHMIC 3D MAZE EXPLORER - AUTOMATED VERIFICATION SUITE                  ");
        System.out.println("================================================================================");

        int total = 0;
        int passed = 0;
        int failed = 0;

        Class<?>[] testClasses = {
            PlayerMovementAndCollisionTest.class,
            AlgorithmVerificationTest.class,
            GameplaySystemsTest.class,
            GameStateAndUITest.class
        };

        for (Class<?> clazz : testClasses) {
            System.out.println("\n>> Executing Test Suite: " + clazz.getSimpleName());

            Method beforeEachMethod = null;
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(BeforeEach.class)) {
                    beforeEachMethod = m;
                    break;
                }
            }

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Test.class)) {
                    total++;
                    try {
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        if (beforeEachMethod != null) {
                            beforeEachMethod.invoke(instance);
                        }
                        m.invoke(instance);
                        passed++;
                        System.out.printf("   [PASS] %-45s%n", m.getName());
                    } catch (Throwable t) {
                        failed++;
                        Throwable cause = t.getCause() != null ? t.getCause() : t;
                        System.err.printf("   [FAIL] %-45s : %s%n", m.getName(), cause.getMessage());
                    }
                }
            }
        }

        System.out.println("\n================================================================================");
        System.out.printf(" TEST SUMMARY: Total: %d | Passed: %d | Failed: %d%n", total, passed, failed);
        if (failed == 0) {
            System.out.println(" RESULT: ALL 3D MOVEMENT & COLLISION TESTS PASSED! (100% SUCCESS)");
        } else {
            System.err.println(" RESULT: " + failed + " TEST(S) FAILED!");
            System.exit(1);
        }
        System.out.println("================================================================================");
    }
}

