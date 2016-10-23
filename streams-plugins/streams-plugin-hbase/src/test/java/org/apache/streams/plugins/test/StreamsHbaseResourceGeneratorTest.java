/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.plugins.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.streams.plugins.hbase.StreamsHbaseGenerationConfig;
import org.apache.streams.plugins.hbase.StreamsHbaseResourceGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static org.apache.streams.util.schema.FileUtil.dropSourcePathPrefix;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsHbaseResourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHbaseResourceGeneratorTest.class);

    public static final Predicate<File> txtFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".txt") )
                return true;
            else return false;
        }
    };

    /**
     * Tests that all example activities can be loaded into Activity beans
     *
     * @throws Exception
     */
    @Test
    public void StreamsHbaseResourceGenerator() throws Exception {

        StreamsHbaseGenerationConfig config = new StreamsHbaseGenerationConfig();

        String sourceDirectory = "target/test-classes/activitystreams-schemas";

        config.setSourceDirectory(sourceDirectory);

        config.setTargetDirectory("target/generated-resources/hbase");

        config.setExclusions(Sets.newHashSet("attachments"));

        config.setColumnFamily("cf");
        config.setMaxDepth(2);

        StreamsHbaseResourceGenerator streamsHbaseResourceGenerator = new StreamsHbaseResourceGenerator(config);
        streamsHbaseResourceGenerator.run();

        File testOutput = config.getTargetDirectory();

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(txtFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );

        String expectedDirectory = "target/test-classes/expected";
        File testExpected = new File( expectedDirectory );

        Iterable<File> expectedIterator = Files.fileTreeTraverser().breadthFirstTraversal(testExpected)
                .filter(txtFilter);
        Collection<File> expectedCollection = Lists.newArrayList(expectedIterator);

        int fails = 0;

        Iterator<File> iterator = expectedCollection.iterator();
        while( iterator.hasNext() ) {
            File objectExpected = iterator.next();
            String expectedEnd = dropSourcePathPrefix(objectExpected.getAbsolutePath(),  expectedDirectory);
            File objectActual = new File(config.getTargetDirectory() + "/" + expectedEnd);
            LOGGER.info("Comparing: {} and {}", objectExpected.getAbsolutePath(), objectActual.getAbsolutePath());
            assert( objectActual.exists());
            if( FileUtils.contentEquals(objectActual, objectExpected) == true ) {
                LOGGER.info("Exact Match!");
            } else {
                LOGGER.info("No Match!");
                fails++;
            }
        }
        if( fails > 0 ) {
            LOGGER.info("Fails: {}", fails);
            Assert.fail();
        }

    }
}