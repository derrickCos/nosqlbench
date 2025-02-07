/*
 * Copyright (c) 2022 nosqlbench
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

package io.nosqlbench.adapter.mongodb.core;

import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import io.nosqlbench.engine.api.activityimpl.OpMapper;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverAdapter;
import io.nosqlbench.engine.api.activityimpl.uniform.DriverSpaceCache;
import io.nosqlbench.engine.api.activityimpl.uniform.flowtypes.Op;
import io.nosqlbench.engine.api.templating.ParsedOp;

import java.util.function.LongFunction;

public class MongodbOpMapper implements OpMapper<Op> {

    private final DriverSpaceCache<? extends MongoSpace> ctxcache;
    private final DriverAdapter adapter;

    public MongodbOpMapper(DriverAdapter adapter, DriverSpaceCache<? extends MongoSpace> ctxcache) {
        this.ctxcache = ctxcache;
        this.adapter = adapter;
    }

    @Override
    public OpDispenser<? extends Op> apply(ParsedOp op) {
        LongFunction<String> ctxNamer = op.getAsFunctionOr("space","default");
        LongFunction<MongoSpace> ctxFunc = l -> ctxcache.get(ctxNamer.apply(l));
        return new MongoOpDispenser(adapter,ctxFunc, op);
    }
}
